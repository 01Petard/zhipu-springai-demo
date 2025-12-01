package com.sangeng.controller;


import com.sangeng.advisor.SGCallAdvisor1;
import com.sangeng.advisor.SGCallAdvisor2;
import com.sangeng.advisor.SimpleMessageChatMemoryAdvisor;
import com.sangeng.entity.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chatclient")
public class ZhipuChatClientController {

    private final ChatClient chatClient;

    //构造器注入
    public ZhipuChatClientController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/simple")
    public String simple(@RequestParam(name = "query") String query) {
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .maxTokens(15536)
                .temperature(0.0)
                .model("glm-4.5")
                .build();

        return chatClient.prompt()
                .system("你是一个有用的AI助手。")
                .user(query)
                .options(options)
                .call().content();
    }

    @GetMapping("/chatResponse")
    public ChatResponse chatResponse(@RequestParam(name = "query") String query) {
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .maxTokens(15536)
                .temperature(0.0)
                .model("glm-4.5")
                .build();

        return chatClient.prompt()
                .system("你是一个有用的AI助手。")
                .user(query)
                .options(options)
                .call().chatResponse();
    }

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


    @GetMapping("/advisor")
    public Book advisor() {
        return chatClient.prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .advisors(new SGCallAdvisor1(), new SGCallAdvisor2())
                .call().entity(Book.class);
    }


    @GetMapping("/simpleMessageChatMemoryAdvisor")
    public String simpleMessageChatMemoryAdvisor(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "conversationId") String conversationId
    ) {
        return chatClient.prompt()
                .user(query)
                //把会话id存入上下文
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .advisors(new SimpleMessageChatMemoryAdvisor())
                .call()
                .content();
    }


}
