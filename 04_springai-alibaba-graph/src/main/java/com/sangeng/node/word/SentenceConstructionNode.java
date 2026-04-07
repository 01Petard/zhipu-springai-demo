package com.sangeng.node.word;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 单词造句 节点
 */
@Component
@RequiredArgsConstructor
public class SentenceConstructionNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        // 从state 中获取 要造句的单词
        String word = state.value("word", "");

        PromptTemplate promptTemplate = new PromptTemplate("""
                你是一个英语造句专家，能够基于给定的单词进行造句。
                要求只返回最终造好的句子，不要返回其他信息。
                给定的单词: %s
                """.formatted(word)
        );
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把句子存入 state
        return Map.of("sentence", content);
    }
}
