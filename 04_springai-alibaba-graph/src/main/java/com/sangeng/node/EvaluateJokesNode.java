package com.sangeng.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class EvaluateJokesNode implements NodeAction {
    private final ChatClient chatClient;

    public EvaluateJokesNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取
        String joke = state.value("joke", "");


        PromptTemplate promptTemplate = new PromptTemplate("你是一个笑话评分专家，能够对笑话进行评分，基于效果的搞笑程度给出0到10分的打分。然后基于评分结果进行评价。如果大于等于3分评价:优秀 否则评价:不够优秀\n" +
                "要求结果只返回最后的评价，不要其他内容。" +
                "要求只返回翻译的结果不要返回其他信息。要评分的笑话：:{joke}");
        promptTemplate.add("joke",joke);
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("result",content.trim());
    }
}
