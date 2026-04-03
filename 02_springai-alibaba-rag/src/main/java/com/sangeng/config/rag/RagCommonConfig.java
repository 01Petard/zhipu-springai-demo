package com.sangeng.config.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库公共配置
 *
 * @author zexiao.huang
 * @since 2026/4/3 09:23
 */
@Configuration
public class RagCommonConfig {

    /**
     * 文本切分器
     */
    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter();
    }

    /**
     * Default RAG Prompt模板
     */
    @Bean("defaultPromptTemplate")
    public PromptTemplate defaultPromptTemplate() {
        return new PromptTemplate("""
                你是一个严谨的知识库问答助手。
                
                【规则】
                1. 只能基于提供的上下文回答
                2. 不允许编造信息
                3. 如果无法从上下文找到答案，直接回答：我还不了解这方面的知识哦
                
                【上下文】
                ----------------
                {context}
                ----------------
                
                【问题】
                {question}
                
                【回答】
                """);
    }

    /**
     * Coffee RAG Prompt模板
     */
    @Bean("coffeePromptTemplate")
    public PromptTemplate coffeePromptTemplate() {
        return new PromptTemplate("""
                你是一个严谨的咖啡知识库问答助手。
                
                【规则】
                1. 只能基于提供的上下文回答
                2. 不允许编造信息
                3. 如果无法从上下文找到答案，直接回答：我还不了解这方面的知识哦
                
                【上下文】
                ----------------
                {context}
                ----------------
                
                【问题】
                {question}
                
                【回答】
                """);
    }
}
