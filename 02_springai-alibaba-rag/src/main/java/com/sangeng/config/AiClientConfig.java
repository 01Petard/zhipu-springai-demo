package com.sangeng.config;

import com.sangeng.advisor.LoggingAdvisor;
import com.sangeng.tool.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型对话配置
 *
 * @author zexiao.huang
 * @since 2026/4/2 20:46
 */
@Configuration
public class AiClientConfig {

    @Bean("defaultChatClient")
    public ChatClient defaultChatClient(
            ChatClient.Builder builder,
            @Qualifier("defaultRagAdvisor") RetrievalAugmentationAdvisor ragAdvisor,
            ToolCallbackProvider toolCallbackProvider
    ) {
        return builder
                .defaultAdvisors(ragAdvisor, new LoggingAdvisor())
                .defaultTools(new TimeTools())
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }

    @Bean("coffeeChatClient")
    public ChatClient coffeeChatClient(
            ChatClient.Builder builder,
            @Qualifier("coffeeRagAdvisor") RetrievalAugmentationAdvisor ragAdvisor,
            ToolCallbackProvider toolCallbackProvider
    ) {
        return builder
                .defaultAdvisors(ragAdvisor, new LoggingAdvisor())
                .defaultTools(new TimeTools())
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }
}