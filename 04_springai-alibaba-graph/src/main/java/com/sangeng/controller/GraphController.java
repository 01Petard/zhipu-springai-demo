package com.sangeng.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private static final Logger log = LoggerFactory.getLogger(GraphController.class);

    @Resource(name = "quickStartGraph")
    private CompiledGraph compiledGraph;

    @GetMapping("/quickStartGraph")
    public void quickStartGraph() {
        Optional<OverAllState> overAllStateOptional = compiledGraph.call(Map.of());
        log.info("overAllStateOptional: {}", overAllStateOptional);
        /*
        OverAllState 示例:
            {
              "data": {
                "input1": "我是1，我经过了节点2",
                "input3": [
                  "我是3",
                  "我叫3，我经过了节点3"
                ],
                "input2": "你好，我的名字是2，我经过了节点2"
              },
              "resume": false,
              "humanFeedback": null,
              "interruptMessage": null
            }
         */
        overAllStateOptional.ifPresent(overAllState -> {
            for (Map.Entry<String, Object> entry : overAllState.data().entrySet()) {
                log.info("{}:{}", entry.getKey(), entry.getValue());
            }
        });
    }

    @Resource(name = "simpleGraph")
    private CompiledGraph simpleGraph;

    @GetMapping("/simpleGraph")
    public Map<String, Object> simpleGraph(@RequestParam("word") String word) {
        Optional<OverAllState> overAllStateOptional = simpleGraph.call(Map.of("word", word));
        return overAllStateOptional.map(OverAllState::data).orElse(Map.of());
        /*
        {
          "sentence": "The sea is vast and blue.",
          "translation": "大海是广阔而蓝色的。",
          "word": "sea"
        }
        {
          "sentence": "Water is essential for life.",
          "translation": "水是生命所必需的。",
          "word": "water"
        }
         */
    }

    @Resource(name = "conditionalGraph")
    private CompiledGraph conditionalGraph;

    @GetMapping("/conditionalGraph")
    public Map<String, Object> conditionalGraph(@RequestParam("topic") String topic) {
        Optional<OverAllState> overAllStateOptional = conditionalGraph.call(Map.of("topic", topic));
        return overAllStateOptional.map(OverAllState::data).orElse(Map.of());
        /*
        {
          "result": "优秀",
          "topic": "过年",
          "joke": "过年时，爸爸给儿子红包，儿子说：\"爸爸，你给得太少了。\"爸爸说：\"别急，这是压岁钱，压住你的岁数，别长得太快！\""
        }
         */
    }

    @Resource(name = "loopGraph")
    private CompiledGraph loopGraph;

    @GetMapping("/loopGraph")
    public Map<String, Object> loopGraph(@RequestParam("topic") String topic) {
        Optional<OverAllState> overAllStateOptional = loopGraph.call(Map.of("topic", topic));
        return overAllStateOptional.map(OverAllState::data).orElse(Map.of());
        /*
        {
          "result": "break",
          "topic": "开车",
          "loopCount": 3,
          "joke": "一个司机被警察拦下，警察说：\"你开得太快了，知道吗？\"司机回答：\"不，我只是在跟着导航的指示。\"警察问：\"导航让你开120？\"司机说：\"不，它说'前方120米右转'。\""
        }
         */
    }


    @Resource(name = "saveGraph")
    private CompiledGraph saveGraph;

    @GetMapping("/saveGraph")
    public Map<String, Object> saveGraph(
            @RequestParam("msg") String msg,
            @RequestParam(value = "sessionId", name = "隔离不同会话") String sessionId
    ) {
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(sessionId)
                .build();
        Optional<OverAllState> overAllStateOptional = saveGraph.call(Map.of("msg", msg), runnableConfig);
        return overAllStateOptional.map(OverAllState::data).orElse(Map.of());
        /*
        {
          "msg": "你好",
          "historyMsg": [
            "你好",
            "你好",
            "你好"
          ]
        }
         */
    }
}
