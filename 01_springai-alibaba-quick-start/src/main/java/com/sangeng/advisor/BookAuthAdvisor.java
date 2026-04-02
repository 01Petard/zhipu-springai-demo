package com.sangeng.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话拦截器
 * @author hzx
 */
@Slf4j
public class BookAuthAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain
    ) {

        List<Message> messages = new ArrayList<>(request.prompt().getInstructions());
        messages.add(0, new SystemMessage("你是一个科幻作家，只写宇宙+爱情"));

        Prompt newPrompt = new Prompt(messages);
        ChatClientRequest newRequest = request.mutate()
                .prompt(newPrompt)
                .build();

        log.info("大模型对话最终Prompt: {}", newPrompt);

        return chain.nextCall(request);
    }

    @Override
    public String getName() {
        return "BookAuthAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
