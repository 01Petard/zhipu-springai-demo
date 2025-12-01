package com.sangeng.controller;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {


    private final VectorStore vectorStore;

    public RagController(VectorStore store) {
        this.vectorStore = store;
    }

    @PostMapping("/importData")
    public String importData(@RequestParam("data") String data) {
        Document document = Document.builder()
                .text(data)
                .build();
        vectorStore.add(List.of(document));

        return "success";
    }

    @PostMapping("/search")
    public List<Document> search(@RequestParam("query") String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(3)
                .query(query)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return documents;
    }
}
