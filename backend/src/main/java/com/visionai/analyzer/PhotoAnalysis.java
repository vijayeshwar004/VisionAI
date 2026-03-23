package com.visionai.analyzer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class PhotoAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFilename;
    private int balanceScore;
    private String compositionType;
    
    @Column(length = 2000)
    private String balanceRemarks;
    
    @Column(length = 2000)
    private String linesRemarks;

    public PhotoAnalysis() {}

    public PhotoAnalysis(String originalFilename, int balanceScore, String compositionType, String balanceRemarks, String linesRemarks) {
        this.originalFilename = originalFilename;
        this.balanceScore = balanceScore;
        this.compositionType = compositionType;
        this.balanceRemarks = balanceRemarks;
        this.linesRemarks = linesRemarks;
    }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public int getBalanceScore() { return balanceScore; }
    public String getCompositionType() { return compositionType; }
    public String getBalanceRemarks() { return balanceRemarks; }
    public String getLinesRemarks() { return linesRemarks; }
}
