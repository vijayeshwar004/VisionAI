package com.visionai.analyzer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;

@Entity
public class PhotoAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFilename;
    private int balanceScore;
    private String compositionType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String strengths; // JSON array of {title, icon, description}

    @Lob
    @Column(columnDefinition = "TEXT")
    private String suggestions; // JSON array of {title, description}

    public PhotoAnalysis() {}

    public PhotoAnalysis(String originalFilename, int balanceScore, String compositionType, String strengths, String suggestions) {
        this.originalFilename = originalFilename;
        this.balanceScore = balanceScore;
        this.compositionType = compositionType;
        this.strengths = strengths;
        this.suggestions = suggestions;
    }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public int getBalanceScore() { return balanceScore; }
    public String getCompositionType() { return compositionType; }
    public String getStrengths() { return strengths; }
    public String getSuggestions() { return suggestions; }
}
