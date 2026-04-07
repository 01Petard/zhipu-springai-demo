package com.sangeng.config;


import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.MergeStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.sangeng.node.joke.JokeEvaluateLoopNode;
import com.sangeng.node.joke.JokeEvaluateNode;
import com.sangeng.node.joke.JokeGenerateNode;
import com.sangeng.node.joke.JokeQualityEnhanceNode;
import com.sangeng.node.word.SentenceConstructionNode;
import com.sangeng.node.word.SentenceTranslationNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.Map;

/**
 * 工作流配置
 */
@Configuration
@RequiredArgsConstructor
public class GraphConfig {

    private static final Logger log = LoggerFactory.getLogger(GraphConfig.class);

    private final JokeGenerateNode jokeGenerateNode;
    private final JokeEvaluateLoopNode jokeEvaluateLoopNode;
    private final JokeEvaluateNode jokeEvaluateNode;
    private final JokeQualityEnhanceNode jokeQualityEnhanceNode;
    private final SentenceTranslationNode sentenceTranslationNode;
    private final SentenceConstructionNode sentenceConstructionNode;

    /**
     * 纯演示状态流转（无 AI，纯逻辑）
     *
     * @return
     * @throws GraphStateException
     */
    @Bean("quickStartGraph")
    @Primary
    public CompiledGraph quickStartGraph() throws GraphStateException {
        // 定义图的策略工厂
        KeyStrategyFactory keyStrategyFactory = () -> Map.of(
                "input1", new ReplaceStrategy(),
                "input2", new MergeStrategy(),
                "input3", new AppendStrategy()
        );

        // 定义状态图 StateGraph
        StateGraph stateGraph = new StateGraph("quickStartGraph", keyStrategyFactory);

        //定义节点
        stateGraph.addNode("node1", AsyncNodeAction.node_async(state -> {
            log.info("node1 state :{}", state);
            return Map.of(
                    "input1", "我是1",
                    "input2", "我是2",
                    "input3", "我是3"
            );
        }));
        stateGraph.addNode("node2", AsyncNodeAction.node_async(state -> {
            log.info("node2 state :{}", state);
            return Map.of(
                    "input1", "我是1，我经过了节点2",
                    "input2", "你好，我的名字是2，我经过了节点2",
                    "input3", "我叫3，我经过了节点3"
            );
        }));

        //定义边
        stateGraph.addEdge(StateGraph.START, "node1");
        stateGraph.addEdge("node1", "node2");
        stateGraph.addEdge("node2", StateGraph.END);

        // 编译状态图
        return stateGraph.compile();
    }


    /**
     * 简单两步流 → 造句 → 翻译
     *
     * @return
     * @throws GraphStateException
     */
    @Bean("simpleGraph")
    public CompiledGraph simpleGraph() throws GraphStateException {
        // 定义图的策略工厂
        KeyStrategyFactory keyStrategyFactory = () -> Map.of(
                "word", new ReplaceStrategy(),
                "sentence", new ReplaceStrategy(),
                "translation", new ReplaceStrategy()
        );
        // 创建状态图
        StateGraph stateGraph = new StateGraph("simpleGraph", keyStrategyFactory);

        // 添加节点
        stateGraph.addNode("SentenceConstructionNode", AsyncNodeAction.node_async(sentenceConstructionNode));
        stateGraph.addNode("SentenceTranslationNode", AsyncNodeAction.node_async(sentenceTranslationNode));

        // 定义边
        stateGraph.addEdge(StateGraph.START, "SentenceConstructionNode");
        stateGraph.addEdge("SentenceConstructionNode", "SentenceTranslationNode");
        stateGraph.addEdge("SentenceTranslationNode", StateGraph.END);

        // 编译状态图
        return stateGraph.compile();
    }

    /**
     * 条件判断笑话（优秀→结束 / 不够优秀→优化）
     *
     * @return
     * @throws GraphStateException
     */
    @Bean("conditionalGraph")
    public CompiledGraph conditionalGraph() throws GraphStateException {
        // 定义图的策略工厂
        KeyStrategyFactory keyStrategyFactory = () -> Map.of(
                "topic", new ReplaceStrategy()
        );

        // 定义状态图 StateGraph
        StateGraph stateGraph = new StateGraph("conditionalGraph", keyStrategyFactory);

        // 定义节点
        stateGraph.addNode("生成笑话", AsyncNodeAction.node_async(jokeGenerateNode));
        stateGraph.addNode("评估笑话", AsyncNodeAction.node_async(jokeEvaluateNode));
        stateGraph.addNode("优化笑话", AsyncNodeAction.node_async(jokeQualityEnhanceNode));

        // 定义边
        stateGraph.addEdge(StateGraph.START, "生成笑话");
        stateGraph.addEdge("生成笑话", "评估笑话");
        stateGraph.addConditionalEdges(
                "评估笑话",
                // 从返回结果state的“result”中获取判断的数据，如果是“优秀”就结束，如果是“不够优秀”就回到“优化笑话”的节点
                AsyncEdgeAction.edge_async(state -> state.value("result", "优秀")),
                // 定义不同的条件结果情况下的后续节点
                Map.of("优秀", StateGraph.END, "不够优秀", "优化笑话")
        );
        stateGraph.addEdge("优化笑话", StateGraph.END);

        // 编译状态图
        return stateGraph.compile();
    }


    /**
     * 循环生成笑话（不合格→重新生成→直到达标）
     *
     * @return
     * @throws GraphStateException
     */
    @Bean("loopGraph")
    public CompiledGraph loopGraph() throws GraphStateException {
        // 定义图的策略工厂
        KeyStrategyFactory keyStrategyFactory = () -> Map.of(
                "topic", new ReplaceStrategy()
        );

        // 定义状态图
        StateGraph stateGraph = new StateGraph("loopGraph", keyStrategyFactory);

        // 定义节点
        stateGraph.addNode("生成笑话", AsyncNodeAction.node_async(jokeGenerateNode));
        stateGraph.addNode("评估笑话", AsyncNodeAction.node_async(jokeEvaluateLoopNode));

        // 定义边
        stateGraph.addEdge(StateGraph.START, "生成笑话");
        stateGraph.addEdge("生成笑话", "评估笑话");
        stateGraph.addConditionalEdges(
                "评估笑话",
                AsyncEdgeAction.edge_async(state -> state.value("result", "loop")),
                Map.of("loop", "生成笑话", "break", StateGraph.END)
        );

        // 编译状态图
        return stateGraph.compile();
    }

    /**
     * 存储节点
     *
     * @return
     * @throws GraphStateException
     */
    @Bean("saveGraph")
    public CompiledGraph saveGraph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = Map::of;

        // 定义状态图 StateGraph
        StateGraph stateGraph = new StateGraph("loopGraph", keyStrategyFactory);
        stateGraph.addNode("对话存储",
                AsyncNodeAction.node_async(state -> {
                    ArrayList<Object> historyMsg = state.value("historyMsg", new ArrayList<>());
                    historyMsg.add(state.value("msg", ""));
                    return Map.of("historyMsg", historyMsg);
                }));

        // 定义边
        stateGraph.addEdge(StateGraph.START, "对话存储");
        stateGraph.addEdge("对话存储", StateGraph.END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "saveGraph");
        log.info("==== expander UML Flow ====");
        log.info("\n{}", representation.content());
        log.info("===========================\n");

        return stateGraph.compile();
    }
}
