package com.sangeng.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rag")
public class RagController {

    private final VectorStore vectorStore;

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
}
