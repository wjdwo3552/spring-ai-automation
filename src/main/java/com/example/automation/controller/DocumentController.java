package com.example.automation.controller;

import com.example.automation.dto.DocumentRequest;
import com.example.automation.dto.SummaryResponse;
import com.example.automation.service.DocumentSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Document", description = "문서 요약 API")
public class DocumentController {

    private final DocumentSummaryService documentSummaryService;

    @Operation(summary = "텍스트 요약", description = "입력된 텍스트를 AI로 요약합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 성공",
                    content = @Content(schema = @Schema(implementation = SummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/summarize")
    public ResponseEntity<SummaryResponse> summarizeDocument(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "요약할 문서 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DocumentRequest.class))
            )
            @RequestBody DocumentRequest request) {

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

    @Operation(summary = "파일 업로드 요약", description = "PDF, DOCX, TXT 등의 파일을 업로드하여 AI로 요약합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 성공",
                    content = @Content(schema = @Schema(implementation = SummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 빈 파일"),
            @ApiResponse(responseCode = "500", description = "파일 처리 중 오류")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndSummarize(
            @Parameter(description = "업로드할 파일 (PDF, DOCX, DOC, TXT, HTML)", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "요약 타입 (short, detailed, bullet_points)", example = "short")
            @RequestParam(value = "summaryType", defaultValue = "short") String summaryType) {

        log.info("Received file upload: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

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

    @Operation(summary = "서비스 상태 확인", description = "문서 서비스가 정상 작동하는지 확인합니다")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Document service is running");
    }
}