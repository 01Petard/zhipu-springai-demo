package com.sangeng.config;


import com.sangeng.tool.TimeTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider getToolCallbackProvider(TimeTools timeTools) {
        return MethodToolCallbackProvider.builder().toolObjects(timeTools).build();
    }
}
