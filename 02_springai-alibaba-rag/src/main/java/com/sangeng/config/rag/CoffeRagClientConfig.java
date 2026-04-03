package com.sangeng.config.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * 咖啡客服知识库配置
 *
 * @author zexiao.huang
 * @since 2026/4/3 09:23
 */
@Configuration
public class CoffeRagClientConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean("coffeeVectorStore")
    public RedisVectorStore coffeeVectorStore(EmbeddingModel embeddingModel) {
        JedisPooled jedisPooled = new JedisPooled(redisHost, redisPort);
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .prefix("rag:coffee:")
                .initializeSchema(true)
                .indexName("rag:coffee:")
                .build();
    }

    @Bean("coffeeRetriever")
    public VectorStoreDocumentRetriever coffeeRetriever(
            @Qualifier("coffeeVectorStore") VectorStore vectorStore
    ) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.8)
                .build();
    }

    @Bean("coffeeRagAdvisor")
    public RetrievalAugmentationAdvisor coffeeRagAdvisor(
            @Qualifier("coffeeRetriever") VectorStoreDocumentRetriever retriever
    ) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();
    }
}
