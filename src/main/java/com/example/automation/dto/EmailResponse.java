package com.example.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private String responseContent;
    private String tone;
    private String suggestedSubject;
}