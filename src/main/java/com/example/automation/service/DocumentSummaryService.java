package com.example.automation.service;

import com.example.automation.dto.DocumentRequest;
import com.example.automation.dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final ChatClient.Builder chatClientBuilder;

    public SummaryResponse summarizeDocument(DocumentRequest request) {
        log.info("Summarizing document with type: {}", request.getSummaryType());

        String promptTemplate = getPromptTemplate(request.getSummaryType());

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(Map.of("content", request.getContent()));

        ChatClient chatClient = chatClientBuilder.build();
        String summary = chatClient.prompt(prompt)
                .call()
                .content();

        return new SummaryResponse(
                summary,
                request.getSummaryType(),
                request.getContent().length(),
                summary.length()
        );
    }

    private String getPromptTemplate(String summaryType) {
        return switch (summaryType) {
            case "short" -> """
                    다음 문서를 2-3문장으로 간단히 요약해주세요:
                    
                    {content}
                    """;
            case "detailed" -> """
                    다음 문서를 상세하게 요약해주세요. 주요 내용과 핵심 포인트를 모두 포함해주세요:
                    
                    {content}
                    """;
            case "bullet_points" -> """
                    다음 문서의 핵심 내용을 bullet point 형식으로 요약해주세요:
                    
                    {content}
                    """;
            default -> """
                    다음 문서를 요약해주세요:
                    
                    {content}
                    """;
        };
    }
}