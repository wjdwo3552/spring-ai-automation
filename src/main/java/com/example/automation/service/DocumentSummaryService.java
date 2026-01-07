package com.example.automation.service;

import com.example.automation.dto.DocumentRequest;
import com.example.automation.dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final ChatClient.Builder chatClientBuilder;

    public SummaryResponse summarizeUploadedFile(MultipartFile file, String summaryType) throws IOException {
        log.info("Summarizing uploaded file: {} with type: {}", file.getOriginalFilename(), summaryType);

        String content = extractTextFromFile(file);

        DocumentRequest request = new DocumentRequest(content, summaryType);
        return summarizeDocument(request);
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        List<Document> documents;

        switch (extension) {
            case ".pdf":
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                        file.getResource(),
                        PdfDocumentReaderConfig.builder()
                                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                                        .withNumberOfBottomTextLinesToDelete(0)
                                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                                        .build())
                                .build()
                );
                documents = pdfReader.get();
                break;

            case ".docx":
            case ".doc":
            case ".txt":
            case ".html":
                TikaDocumentReader tikaReader = new TikaDocumentReader(file.getResource());
                documents = tikaReader.get();
                break;

            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }

        StringBuilder fullText = new StringBuilder();
        for (Document doc : documents) {
            fullText.append(doc.getContent()).append("\n\n");
        }

        return fullText.toString().trim();
    }

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