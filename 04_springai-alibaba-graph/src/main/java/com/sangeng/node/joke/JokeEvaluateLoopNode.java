package com.sangeng.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 笑话循环评估 节点
 */
@Component
@RequiredArgsConstructor
public class JokeEvaluateLoopNode implements NodeAction {

    private static final Logger log = LoggerFactory.getLogger(JokeEvaluateLoopNode.class);

    private final static Integer TARGET_SCORE = 7;
    private final static Integer MAX_LOOP = 3;

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        // 从state 中获取
        String joke = state.value("joke", "");
        Integer loopCount = state.value("loopCount", 1);
        PromptTemplate promptTemplate = new PromptTemplate(
                """
                你是一个笑话评分专家，基于笑话的搞笑程度给出0到10分的打分。
                要求只返回一个整数的评分，不要其他内容。
                要评分的笑话: %s
                """.formatted(joke));
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        Integer score = Integer.valueOf(content.trim());
        log.info("joke: {}, score: {}, loopCount: {}", joke, score, loopCount);
        // 根据分数判断要继续循环还是结束  循环最多执行10次
        String result = score >= TARGET_SCORE || loopCount >= MAX_LOOP ? "break" : "loop";
        loopCount++;
        // 把翻译结果 存入 state
        return Map.of(
                "result", result,
                "loopCount", loopCount
        );
    }
}
