package com.sangeng.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {

    @Resource(name = "defaultVectorStore")
    private VectorStore vectorStore;

    @Resource(name = "defaultChatClient")
    private ChatClient chatClient;

    @Resource(name = "defaultPromptTemplate")
    private PromptTemplate promptTemplate;

    @Operation(summary = "构建知识库")
    @PostMapping("/importData")
    public void importData(
            @RequestParam("data") String data
    ) {
        Document document = Document.builder()
                .text(data)
                .build();
        vectorStore.add(List.of(document));
    }

    @Operation(summary = "查询知识库最匹配的文档")
    @PostMapping("/search")
    public List<Document> search(
            @RequestParam("query") String query
    ) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(3)
                .query(query)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }


    @GetMapping("/chat")
    public String chat(
            @RequestParam("query") String query
    ) {
        // 1. 向量检索
        List<Document> documents = vectorStore.similaritySearch(query);
        documents.forEach(document -> {
            log.info("找到默认的知识：{}", document);
        });

        // 2. 拼 context
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 3. 用 PromptTemplate
        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", query
        ));

        // 4. 调 LLM 对话
        try {
            return chatClient.prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("调用大模型对话失败: {}，对话内容: {}", e.getMessage(), prompt);
            // 提取异常中的关键信息
            String errorMessage = extractErrorMessage(e);
            return "抱歉，处理您的请求时出现了问题：" + errorMessage;
        }
    }

    /**
     * 从异常中提取友好的错误信息
     */
    private String extractErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "未知错误";
        }
        // 处理智谱 AI 的错误信息
        if (message.contains("1301")) {
            return "您的请求可能包含敏感内容，请尝试更换问题或稍后再试。";
        }
        if (message.contains("contentFilter")) {
            return "请求内容可能包含敏感信息，请修改后重试。";
        }
        if (message.contains("HTTP 400") || message.contains("HTTP 500")) {
            return "AI 服务暂时不可用，请稍后再试。";
        }
        // 截断过长的错误信息
        if (message.length() > 100) {
            return message.substring(0, 100) + "...";
        }
        return message;
    }
}
