package com.sangeng.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/coffee")
public class CoffeeController {

    @Resource
    @Qualifier("coffeeVectorStore")
    private VectorStore vectorStore;

    @Resource
    @Qualifier("coffeeChatClient")
    private ChatClient chatClient;

    @Resource
    @Qualifier("coffeePromptTemplate")
    private PromptTemplate promptTemplate;

    @Operation(summary = "构建知识库")
    @GetMapping("/import")
    public String importData() {
        // 读取classpath下的QA.csv文件
        ClassPathResource resource = new ClassPathResource("QA.csv");

        // 使用Apache Commons CSV解析CSV文件
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream());
             CSVParser csvParser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            List<Document> documents = new ArrayList<>();
            // 遍历每一行记录
            for (CSVRecord record : csvParser) {
                // 获取问题和回答字段
                String question = record.get("问题");
                String answer = record.get("回答");
                // 将问题和回答组合成文档内容
                String content = "问题: " + question + "\n回答: " + answer;
                log.info("添加知识：{}", content);
                // 创建Document对象，添加到文档列表
                Document document = new Document(content);
                documents.add(document);
            }

            // 将文档存入向量数据库
            vectorStore.add(documents);
            return "成功导入 " + documents.size() + " 条记录到向量数据库";

        } catch (IOException e) {
            return "导入失败: " + e.getMessage();
        }
    }

    @GetMapping("/chat")
    public String ragAskQuestion(
            @RequestParam("query") String query
    ) {
        // 1. 向量检索
        List<Document> documents = vectorStore.similaritySearch(query);
        documents.forEach(document -> {
            log.info("找到咖啡相关的知识：{}", document);
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


    @GetMapping("/fetcher")
    public String fetcher(
            @RequestParam("question") String question
    ) {
        return chatClient.prompt()
                .system("你是一个网页爬取专家，你可以运用工具爬取指定网页的内容并且进行总结。")
                .user(question)
                .call().content();

    }
}