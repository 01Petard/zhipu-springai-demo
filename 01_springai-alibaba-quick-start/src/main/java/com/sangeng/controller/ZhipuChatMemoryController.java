package com.sangeng.controller;


import com.sangeng.advisor.SGCallAdvisor1;
import com.sangeng.advisor.SGCallAdvisor2;
import com.sangeng.advisor.SimpleMessageChatMemoryAdvisor;
import com.sangeng.entity.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/chatMemory")
public class ZhipuChatMemoryController {

    private final ChatClient chatClient;
    //构造器注入
    public ZhipuChatMemoryController(ChatClient.Builder builder) {

        // 创建 MessageWindowChatMemory
        MessageWindowChatMemory windowChatMemory = MessageWindowChatMemory.builder()
                .build();

        //创建 MessageChatMemoryAdvisor
        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(windowChatMemory)
                .build();


        this.chatClient = builder
                .defaultAdvisors(chatMemoryAdvisor)
                .build();
    }


    @GetMapping("/simpleMessageChatMemoryAdvisor")
    public String simpleMessageChatMemoryAdvisor(@RequestParam(name = "query") String query,
                                                 @RequestParam(name = "conversationId") String conversationId) {
        return chatClient.prompt()
                .user(query)
                //把会话id存入上下文
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .advisors(new SimpleMessageChatMemoryAdvisor())
                .call()
                .content();
    }

    @GetMapping("/messageChatMemoryAdvisor")
    public String messageChatMemoryAdvisor(@RequestParam(name = "query") String query,
                                                 @RequestParam(name = "conversationId") String conversationId) {

        return chatClient.prompt()
                .user(query)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public static void main(String[] args) {
        // 创建UserMessage
        PromptTemplate userPrompt = new PromptTemplate("你是一个有用的人工智能助手，名字是{name}请用{voice}的风格回答以下问题：{userQuestion}");
        Message message = userPrompt.createMessage(Map.of("name", "小白", "voice", "幽默", "userQuestion", "推荐上海的三个景点"));
        System.out.println(message);


        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("你是一个有用的人工智能助手，名字是{name}请用{voice}的风格回答以下问题：{userQuestion}");
        Message message2 = systemPromptTemplate.createMessage(Map.of("name", "小白", "voice", "幽默", "userQuestion", "推荐上海的三个景点"));
        System.out.println(message2);
    }

}
