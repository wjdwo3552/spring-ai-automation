package com.example.automation.controller;

import com.example.automation.dto.DocumentRequest;
import com.example.automation.dto.SummaryResponse;
import com.example.automation.service.DocumentSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndSummarize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "summaryType", defaultValue = "short") String summaryType) {

        log.info("Received file upload: {}", file.getOriginalFilename());

        // 파일 검증
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // 파일 형식 검증
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidFileType(filename)) {
            return ResponseEntity.badRequest()
                    .body("Unsupported file format. Supported: PDF, DOCX, DOC, TXT, HTML");
        }

        try {
            SummaryResponse response = documentSummaryService.summarizeUploadedFile(file, summaryType);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Error processing file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean isValidFileType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return extension.equals(".pdf") ||
                extension.equals(".docx") ||
                extension.equals(".doc") ||
                extension.equals(".txt") ||
                extension.equals(".html");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Document service is running");
    }
}