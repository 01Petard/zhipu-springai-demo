package com.sangeng.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 笑话点评 节点
 */
@Component
@RequiredArgsConstructor
public class JokeEvaluateNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取
        String joke = state.value("joke", "");


        PromptTemplate promptTemplate = new PromptTemplate(
                """
                你是一个笑话评分专家，基于笑话的搞笑程度给出0到10分，再进行评价。
                如果大于等于3分则评价: 优秀，否则评价: 不够优秀。
                结果只返回最后的评价，不要其他内容。
                要评分的笑话: %s
                """.formatted(joke));
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 把翻译结果 存入 state
        return Map.of("result", content.trim());
    }
}
