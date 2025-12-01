package com.sangeng.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class GenerateJokeNode implements NodeAction {
    private final ChatClient chatClient;

    public GenerateJokeNode(ChatClient.Builder builder) {

        this.chatClient = builder.build();

    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取
        String topic = state.value("topic", "");


        PromptTemplate promptTemplate = new PromptTemplate("你需要写一个关于指定主题的短笑话。要求返回的结果中只能包含笑话的内容" +
                "主题:{topic}");
        promptTemplate.add("topic",topic);
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("joke",content);
    }
}
