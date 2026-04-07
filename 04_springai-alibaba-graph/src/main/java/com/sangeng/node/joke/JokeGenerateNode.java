package com.sangeng.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 笑话生成 节点
 */
@Component
@RequiredArgsConstructor
public class JokeGenerateNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取
        String topic = state.value("topic", "");


        PromptTemplate promptTemplate = new PromptTemplate(
                """
                你需要写一个关于指定主题的短笑话。
                要求返回的结果中只能包含笑话的内容。
                主题: %s
                """.formatted(topic));
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("joke", content);
    }
}
