package com.sangeng.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

@Slf4j
public class ChatTimeCalcuAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain
    ) {
        long start = System.currentTimeMillis();
        ChatClientResponse response = chain.nextCall(request);
        long cost = System.currentTimeMillis() - start;
        log.info("大模型对话耗时: {}s", (double) cost / 1000);

        return response;
    }

    @Override
    public String getName() {
        return "ChatTimeCalcuAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
