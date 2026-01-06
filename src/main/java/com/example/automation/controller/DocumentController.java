package com.example.automation.controller;

import com.example.automation.dto.DocumentRequest;
import com.example.automation.dto.SummaryResponse;
import com.example.automation.service.DocumentSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentSummaryService documentSummaryService;

    @PostMapping("/summarize")
    public ResponseEntity<SummaryResponse> summarizeDocument(@RequestBody DocumentRequest request) {
        log.info("Received document summarization request");

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getSummaryType() == null) {
            request.setSummaryType("short");
        }

        SummaryResponse response = documentSummaryService.summarizeDocument(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Document service is running");
    }
}