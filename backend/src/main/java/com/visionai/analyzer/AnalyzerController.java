package com.visionai.analyzer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows React frontend to communicate seamlessly
public class AnalyzerController {

    @Autowired
    private PhotoAnalysisRepository repository;

    @PostMapping("/analyze")
    public PhotoAnalysis analyzeImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            throw new RuntimeException("Uploaded file was empty.");
        }

        // Simulate advanced composition analysis heuristics
        Random random = new Random();
        int score = 75 + random.nextInt(23); // generates realistic high score
        
        String compType = score > 88 ? "Asymmetrical" : "Symmetrical Core";
        String balanceRemark = score > 88 
            ? "The composition uses asymmetrical balance very effectively, placing heavy visual weight naturally offset from the center."
            : "The composition revolves around strong central symmetry, providing a grounded, stable visual foundation.";
            
        String linesRemark = "Natural leading lines draw the viewer’s eye seamlessly through the foreground into the background, enhancing three-dimensional depth.";

        PhotoAnalysis analysis = new PhotoAnalysis(image.getOriginalFilename(), score, compType, balanceRemark, linesRemark);
        
        // Save metadata and response persistently to DB
        return repository.save(analysis);
    }
}
