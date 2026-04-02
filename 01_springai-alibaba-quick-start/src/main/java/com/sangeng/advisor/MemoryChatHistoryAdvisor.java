package com.sangeng.advisor;

import com.sangeng.constants.BizConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于内存实现的历史对话记忆
 *
 * @author hzx
 */
@Slf4j
public class MemoryChatHistoryAdvisor implements BaseAdvisor {

    // 最简单的实现，基于内存存储
    private static final Map<String, List<Message>> CHAT_MEMORY = new HashMap<>();


    /*
    完整流程：
        用户请求
           ↓
        before（注入历史）
           ↓
        模型调用
           ↓
        after（保存AI回复）
           ↓
        返回结果
     */
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        log.info("对话开始，开始注入历史记忆...");

        // 通过会话id查询之前的对话记录
        String conversationId = (String) request.context().get(ChatMemory.CONVERSATION_ID);
        List<Message> messages = CHAT_MEMORY.get(conversationId);
        if (messages == null) {
            messages = new ArrayList<>();
        }
        // 把这次请求的消息添加到对话记录中
        messages.addAll(request.prompt().getInstructions());
        CHAT_MEMORY.put(conversationId, messages);
        // 把添加后记录的List<Message> 放入请求中
        Prompt newPrompt = request.prompt().mutate()
                .messages(messages)
                .build();
        ChatClientRequest chatClientRequest = request.mutate()
                .prompt(newPrompt)
                .build();
        log.info("对话开始，完成注入历史记忆: {}", chatClientRequest);

        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        log.info("对话完成，开始保存AI回复...");

        // 通过会话id查询之前的对话记录
        String conversationId = (String) response.context().get(ChatMemory.CONVERSATION_ID);
        List<Message> historyMessages = CHAT_MEMORY.get(conversationId);
        if (historyMessages == null) {
            historyMessages = new ArrayList<>();
        }
        // 获取response中ai的消息 添加到对话记录中
        AssistantMessage assistantMessage = response.chatResponse()
                .getResult()
                .getOutput();
        historyMessages.add(assistantMessage);
        CHAT_MEMORY.put(conversationId, historyMessages);

        log.info("对话完成，完成保存AI回复: {}", response);
        return response;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
