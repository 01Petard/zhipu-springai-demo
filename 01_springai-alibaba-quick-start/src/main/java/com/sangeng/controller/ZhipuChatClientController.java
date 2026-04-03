package com.sangeng.controller;


import com.sangeng.advisor.BookAuthAdvisor;
import com.sangeng.advisor.ChatTimeCalcuAdvisor;
import com.sangeng.advisor.MemoryChatHistoryAdvisor;
import com.sangeng.constants.BizConstants;
import com.sangeng.entity.Book;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/chatclient")
public class ZhipuChatClientController {

    private final ChatClient chatClient;

    //构造器注入
    public ZhipuChatClientController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                        MessageWindowChatMemory.builder()
                                                .build()
                                )
                                .build()
                )
                .build();
    }


    @GetMapping("/simple")
    public String simple(@RequestParam(name = "query") String query) {
        return chatClient.prompt()
                .system(BizConstants.DEFAULT_SYSTEM_PROMPT)
                .user(query)
                .options(
                        ZhiPuAiChatOptions.builder()
                                .maxTokens(15536)
                                .temperature(0.0)
                                .model("glm-4.5")
                                .build()
                )
                .call().content();
    }

    @GetMapping("/chatResponse")
    public ChatResponse chatResponse(@RequestParam(name = "query") String query) {
        return chatClient.prompt()
                .system(BizConstants.DEFAULT_SYSTEM_PROMPT)
                .user(query)
                .options(
                        ZhiPuAiChatOptions.builder()
                                .maxTokens(15536)
                                .temperature(0.0)
                                .model("glm-4.5")
                                .build()
                )
                .call().chatResponse();
    }

    @Operation(summary = "根据实体类建模，返回实体类参数", description = "根据实体类建模，返回实体类参数")
    @GetMapping("/entity")
    public Book response() {
        return chatClient.prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .call().entity(Book.class);
    }


    @GetMapping("/stream")
    public Flux<String> stream() {
        return chatClient.prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .stream()
                .content();
    }

    @GetMapping(value = "/stream/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream2() {
        return chatClient.prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .stream()
                .content()
                .mapNotNull(content -> ServerSentEvent.builder(content).build());
    }


    @GetMapping("/advisor")
    public Book advisor() {
        return chatClient.prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .advisors(new BookAuthAdvisor(), new ChatTimeCalcuAdvisor())
                .call().entity(Book.class);
    }


    @GetMapping("/advisor/memory")
    public String memory(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "conversationId") String conversationId
    ) {
        return chatClient.prompt()
                .user(query)
                //把会话id存入上下文
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .advisors(new MemoryChatHistoryAdvisor())
                .call()
                .content();
    }

    @GetMapping("/advisor/mix")
    public String advisor2(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "conversationId") String conversationId
    ) {
        return chatClient.prompt()
                .user(query)

                .advisors(advisorSpec ->
                        advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .advisors(new MemoryChatHistoryAdvisor())
                .advisors(new BookAuthAdvisor(), new ChatTimeCalcuAdvisor())

                .call()
                .content();
    }

    public static void main(String[] args) {
        // 创建UserMessage
        PromptTemplate userPrompt = new PromptTemplate("你是一个有用的人工智能助手，名字是{name}请用{voice}的风格回答以下问题：{userQuestion}");
        Message message = userPrompt.createMessage(
                Map.of(
                        "name", "小白",
                        "voice", "幽默",
                        "userQuestion", "推荐上海的三个景点"
                )
        );
        System.out.println(message);


        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("你是一个有用的人工智能助手，名字是{name}请用{voice}的风格回答以下问题：{userQuestion}");
        Message message2 = systemPromptTemplate.createMessage(
                Map.of(
                        "name", "小白",
                        "voice", "幽默",
                        "userQuestion", "推荐上海的三个景点"
                )
        );
        System.out.println(message2);
    }


}
