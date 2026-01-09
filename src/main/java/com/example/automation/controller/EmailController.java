package com.example.automation.controller;

import com.example.automation.dto.EmailRequest;
import com.example.automation.dto.EmailResponse;
import com.example.automation.service.EmailResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Tag(name = "Email", description = "이메일 자동 응답 API")
public class EmailController {

    private final EmailResponseService emailResponseService;

    @Operation(summary = "이메일 자동 응답 생성",
            description = "받은 이메일에 대한 자동 응답을 AI로 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "응답 생성 성공",
                    content = @Content(schema = @Schema(implementation = EmailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/auto-reply")
    public ResponseEntity<EmailResponse> generateAutoReply(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이메일 정보 (발신자, 제목, 내용, 톤)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EmailRequest.class))
            )
            @RequestBody EmailRequest request) {

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

    @Operation(summary = "서비스 상태 확인",
            description = "이메일 서비스가 정상 작동하는지 확인합니다")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email service is running");
    }
}