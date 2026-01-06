package com.example.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private String summary;
    private String summaryType;
    private int originalLength;
    private int summaryLength;
}