package com.example.automation.controller;

import com.example.automation.dto.EmailRequest;
import com.example.automation.dto.EmailResponse;
import com.example.automation.service.EmailResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailResponseService emailResponseService;

    @PostMapping("/auto-reply")
    public ResponseEntity<EmailResponse> generateAutoReply(@RequestBody EmailRequest request) {
        log.info("Received email auto-reply request");

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getTone() == null) {
            request.setTone("professional");
        }

        EmailResponse response = emailResponseService.generateEmailResponse(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email service is running");
    }
}