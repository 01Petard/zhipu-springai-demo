package com.sangeng.node.word;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 句子翻译 节点
 */
@Component
@RequiredArgsConstructor
public class SentenceTranslationNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        // 从state 中获取 要翻译的句子
        String sentence = state.value("sentence", "");

        PromptTemplate promptTemplate = new PromptTemplate("""
                你是一个英语翻译专家，能够对句子进行翻译。
                要求只返回翻译的结果不要返回其他信息。
                要翻译的句子: %s
                """.formatted(sentence));
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("translation", content);
    }
}
