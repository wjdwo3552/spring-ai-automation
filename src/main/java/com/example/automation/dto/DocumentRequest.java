package com.example.automation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문서 요약 요청")
public class DocumentRequest {

    @Schema(description = "요약할 문서 내용", example = "인공지능은 컴퓨터가 인간처럼 생각하고 학습하는 기술입니다...")
    private String content;

    @Schema(description = "요약 타입", example = "short", allowableValues = {"short", "detailed", "bullet_points"})
    private String summaryType;
}