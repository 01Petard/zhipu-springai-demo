package com.sangeng.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

/**
 * @author zexiao.huang
 * @since 2026/4/3 09:25
 */
@Slf4j
public class LoggingAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

        log.info("=== AI Request ===");
        log.info("Prompt: {}", request.prompt());

        long start = System.currentTimeMillis();

        ChatClientResponse response = chain.nextCall(request);

        long cost = System.currentTimeMillis() - start;

        log.info("=== AI Response ===");
        log.info("Cost: {} ms", cost);
        log.info("Response: {}", response);

        return response;
    }

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
