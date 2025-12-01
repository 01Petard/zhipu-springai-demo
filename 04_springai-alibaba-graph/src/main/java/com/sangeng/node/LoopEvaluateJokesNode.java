package com.sangeng.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class LoopEvaluateJokesNode implements NodeAction {
    private static final Logger log = LoggerFactory.getLogger(LoopEvaluateJokesNode.class);
    private final ChatClient chatClient;
    private final Integer targetScore ;
    private final Integer maxLoopCount ;
    public LoopEvaluateJokesNode(ChatClient.Builder builder, Integer targetScore, Integer maxLoopCount) {
        this.chatClient = builder.build();
        this.targetScore = targetScore;
        this.maxLoopCount = maxLoopCount;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 从state 中获取
        String joke = state.value("joke", "");
        Integer loopCount = state.value("loopCount", 1);
        PromptTemplate promptTemplate = new PromptTemplate("你是一个笑话评分专家，能够对笑话进行评分，基于效果的搞笑程度给出0到10分的打分。要求打分只能是整数\n" +
                "要求结果只返回最后的打分，不要其他内容。" +
                "要评分的笑话：:{joke}");
        promptTemplate.add("joke",joke);
        String prompt = promptTemplate.render();
        // 模型调用
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        // content 转化成 整数
        Integer score = Integer.valueOf(content.trim());
        log.info("joke {} ,score {} ,loopCount {}", joke, score,loopCount);
        // 根据分数判断要继续循环还是结束  循环最多执行10次
        String result = "loop";
        if(score>=targetScore||loopCount>=maxLoopCount){
            result = "break";
        }
        loopCount++;
        // 把翻译结果 存入 state
        return Map.of("result",result,"loopCount",loopCount);
    }
}
