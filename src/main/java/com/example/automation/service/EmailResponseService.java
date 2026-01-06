package com.example.automation.service;

import com.example.automation.dto.EmailRequest;
import com.example.automation.dto.EmailResponse;
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
public class EmailResponseService {

    private final ChatClient.Builder chatClientBuilder;

    public EmailResponse generateEmailResponse(EmailRequest request) {
        log.info("Generating email response with tone: {}", request.getTone());

        String promptTemplate = getPromptTemplate(request.getTone());

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(Map.of(
                "subject", request.getSubject(),
                "content", request.getContent(),
                "senderEmail", request.getSenderEmail()
        ));

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt(prompt)
                .call()
                .content();

        return new EmailResponse(
                response,
                request.getTone(),
                "Re: " + request.getSubject()
        );
    }

    private String getPromptTemplate(String tone) {
        String basePrompt = """
                다음 이메일에 대한 답장을 작성해주세요.
                
                발신자: {senderEmail}
                제목: {subject}
                내용:
                {content}
                
                """;

        String toneInstruction = switch (tone) {
            case "professional" -> "전문적이고 격식있는 톤으로 답장을 작성해주세요.";
            case "friendly" -> "친근하고 편안한 톤으로 답장을 작성해주세요.";
            case "formal" -> "매우 격식있고 공식적인 톤으로 답장을 작성해주세요.";
            default -> "적절한 톤으로 답장을 작성해주세요.";
        };

        return basePrompt + toneInstruction;
    }
}