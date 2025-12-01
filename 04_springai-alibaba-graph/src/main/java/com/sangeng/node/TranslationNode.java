package com.sangeng.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class TranslationNode implements NodeAction {
    private final ChatClient chatClient;

    public TranslationNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取 要翻译的句子
        String sentence = state.value("sentence", "");


        PromptTemplate promptTemplate = new PromptTemplate("你是一个英语翻译专家，能够对句子进行翻译。" +
                "要求只返回翻译的结果不要返回其他信息。要翻译的句子:{sentence}");
        promptTemplate.add("sentence",sentence);
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("translation",content);
    }
}
