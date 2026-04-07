package com.sangeng.config;

import org.springframework.ai.chat.client.ChatClient;
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

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder
    ) {
        return builder.build();
    }


}