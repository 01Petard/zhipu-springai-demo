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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {

    @Resource
    @Qualifier("defaultVectorStore")
    private VectorStore vectorStore;

    @Resource
    @Qualifier("defaultChatClient")
    private ChatClient chatClient;

    @Resource
    @Qualifier("defaultPromptTemplate")
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
        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
