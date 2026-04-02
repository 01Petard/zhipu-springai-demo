package com.sangeng.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/coffee")
public class CoffeeController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

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

    /**
     * 新增的RAG问答接口，明确展示查询向量数据库的过程
     * @param question 用户的问题
     * @return AI基于检索到的信息生成的回答
     */
    @GetMapping("/rag-ask")
    public String ragAskQuestion(
            @RequestParam("question") String question
    ) {
        return chatClient.prompt()
                .system("你是三更咖啡的服务员，你需要回答用户的问题。")
                .user(question)
                .call().content();

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