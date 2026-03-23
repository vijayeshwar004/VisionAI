package com.visionai.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    // Try multiple models in order of preference
    private static final List<String> MODELS = List.of(
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-2.0-flash-lite"
    );

    private static final String ANALYSIS_PROMPT = """
            You are an expert photography composition analyzer. Analyze the uploaded image and return a JSON object with the following structure, no markdown, ONLY raw JSON:
            {
              "overallScore": <integer 0-100>,
              "compositionType": "<string: e.g. Asymmetrical, Symmetrical, Centered, Rule of Thirds, Golden Ratio>",
              "strengths": [
                {
                  "title": "<short title>",
                  "icon": "<single emoji>",
                  "description": "<2-3 sentence analysis of this specific strength in the photo>"
                }
              ],
              "suggestions": [
                {
                  "title": "<short title>",
                  "description": "<2-3 sentence actionable suggestion to improve this specific aspect>"
                }
              ]
            }

            Rules:
            - Provide exactly 4 strengths and 3 suggestions
            - Be specific to THIS photo, not generic advice
            - overallScore should honestly reflect composition quality
            - Evaluate: rule of thirds, leading lines, balance, depth/layering, color harmony, lighting, framing, focal point
            - Return ONLY valid JSON, no explanation text before or after
            """;

    public PhotoAnalysis analyzeImage(MultipartFile image) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured. Set gemini.api.key in application.properties");
        }

        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        String requestBody = objectMapper.writeValueAsString(
            new GeminiRequest(base64Image, mimeType, ANALYSIS_PROMPT)
        );

        // Try each model until one succeeds
        String lastError = "";
        for (String model : MODELS) {
            String url = BASE_URL + model + ":generateContent?key=" + apiKey;

            // Retry up to 3 times per model with backoff for rate limits
            for (int attempt = 0; attempt < 3; attempt++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseGeminiResponse(response.body(), image.getOriginalFilename());
                } else if (response.statusCode() == 429) {
                    // Rate limited - wait and retry
                    lastError = "Rate limited on model " + model;
                    Thread.sleep((attempt + 1) * 5000L); // 5s, 10s, 15s backoff
                    continue;
                } else {
                    lastError = "Model " + model + " returned HTTP " + response.statusCode();
                    break; // Don't retry non-rate-limit errors on same model
                }
            }
        }

        throw new RuntimeException("All Gemini models failed. Last error: " + lastError +
            ". Your free-tier quota may be exhausted for today. Try again later or check https://ai.dev/rate-limit");
    }

    private PhotoAnalysis parseGeminiResponse(String responseBody, String filename) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Clean any markdown wrapping
        text = text.strip();
        if (text.startsWith("```json")) text = text.substring(7);
        if (text.startsWith("```")) text = text.substring(3);
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
        text = text.strip();

        JsonNode analysis = objectMapper.readTree(text);

        int score = analysis.path("overallScore").asInt(75);
        String compType = analysis.path("compositionType").asText("Unknown");
        String strengths = objectMapper.writeValueAsString(analysis.path("strengths"));
        String suggestions = objectMapper.writeValueAsString(analysis.path("suggestions"));

        return new PhotoAnalysis(filename, score, compType, strengths, suggestions);
    }

    // Inner classes for Gemini API request structure
    record Part(String text) {}
    record InlineData(String mimeType, String data) {}
    record ImagePart(InlineData inlineData) {}
    record Content(Object[] parts) {}
    record GeminiRequest(Content[] contents) {
        GeminiRequest(String base64Image, String mimeType, String prompt) {
            this(new Content[]{
                new Content(new Object[]{
                    new ImagePart(new InlineData(mimeType, base64Image)),
                    new Part(prompt)
                })
            });
        }
    }
}
