package com.sangeng.config;

import com.sangeng.tool.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zexiao.huang
 * @since 2026/4/2 20:46
 */
@Configuration
public class AiClientConfig {



    @Bean
    public ChatClient chatClient(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider
    ) {

        // 向量检索器（RAG）
        VectorStoreDocumentRetriever retriever =
                VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build();

        // RAG增强顾问
        RetrievalAugmentationAdvisor ragAdvisor =
                RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(retriever)
                        .build();

        // 构建 ChatClient
        return chatClientBuilder
                .defaultAdvisors(ragAdvisor)
                .defaultTools(new TimeTools())
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }
}