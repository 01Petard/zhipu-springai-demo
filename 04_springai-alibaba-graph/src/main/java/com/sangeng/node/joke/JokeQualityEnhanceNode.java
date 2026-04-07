package com.sangeng.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 笑话优化 节点
 */
@Component
@RequiredArgsConstructor
public class JokeQualityEnhanceNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取
        String joke = state.value("joke", "");

        PromptTemplate promptTemplate = new PromptTemplate(
                """
                你是一个笑话优化专家，你能够优化笑话，让它更加搞笑
                要求只返回翻译的结果，不要返回其他信息。
                要优化的笑话: %s
                """.formatted(joke));
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("newJoke",content);
    }
}
