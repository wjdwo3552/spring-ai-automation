package com.example.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String senderEmail;
    private String subject;
    private String content;
    private String tone; // "professional", "friendly", "formal"
}