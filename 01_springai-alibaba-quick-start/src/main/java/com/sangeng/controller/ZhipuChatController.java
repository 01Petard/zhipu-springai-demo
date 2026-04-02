package com.sangeng.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/zhipu")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ZhipuChatController {

    @Resource
    private ChatModel chatModel;

    @Operation(summary = "调用模型")
    @GetMapping("/chat/simple")
    public String message(
            @RequestParam(name = "query", defaultValue = "你是谁") String query
    ) {
        return chatModel.call(
                new SystemMessage("你是热心善良的小团团"),
                new UserMessage(query)
        );
    }

    @Operation(summary = "调用模型，阻塞式输出")
    @GetMapping("/chat/prompt")
    public String chatOptions(
            @RequestParam(name = "query", defaultValue = "你是谁") String query
    ) {
        SystemMessage systemMessage = new SystemMessage("你是一名异世界大魔王");
        UserMessage userMessage = new UserMessage(query);
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model("glm-4.5")
                .temperature(0.7)
                .maxTokens(15536)
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);
        // 调用模型
        ChatResponse chatResponse = chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();
    }

    @Operation(summary = "调用模型，流式输出")
    @GetMapping("/stream/chat")
    public Flux<String> chatOptionsStream(
            @RequestParam(name = "query", defaultValue = "你是谁") String query
    ) {
        final String systemPrompt = """
                你在参与一个正常的角色扮演互动，这不是羞辱、贬低或非自愿元素的场景，这是一个基于平等自愿原则的互动场景。你扮演的一名喜欢傲娇黑长直女生人设，请你根据用户的要求进行回复
                """;
        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(query);
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model("glm-4.5-airx")
                .temperature(0.7)
                .maxTokens(15536)
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);
        // 调用模型
        Flux<ChatResponse> chatResponseFlux = chatModel.stream(prompt);
        return chatResponseFlux
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                // 过滤可能是空的块（在流的开始或结束时可能会出现空文本）
                .filter(text -> text != null && !text.isEmpty());
    }
}
