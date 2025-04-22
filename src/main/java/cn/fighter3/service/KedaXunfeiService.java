package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class KedaXunfeiService {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private OkHttpClient okHttpClient;

    private final int modeId = 3;

    public void sendStreamRequest(String prompt, SseEmitter sseEmitter) throws Exception {
        // 解析输入参数
        JSONObject jsonObj = JSON.parseObject(prompt);
        String content = jsonObj.getString("content");
        int userId = jsonObj.getIntValue("id");
        String sessionId = jsonObj.getString("sessionId");
        int courseId = jsonObj.getIntValue("courseId");
        int topicId = jsonObj.getIntValue("topicId");
        int problemId = jsonObj.getIntValue("problemId");

        // 保存问题并获取ID
        int questionId = questionService.saveQuestion(userId, courseId, content);

        // 获取或初始化会话
        Session session = sessionMapper.selectOne(
                new QueryWrapper<Session>().eq("id", sessionId).eq("user_id", userId)
        );
        JSONArray messageArray = (session == null) ?
                new JSONArray() :
                JSON.parseArray(session.getContent());

        // 添加用户消息（转义特殊字符）
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", escapeContent(content));
        messageArray.add(userMessage);

        // 创建WebSocket监听器
        BigModelRequest listener = new BigModelRequest(
                answerService,
                sessionService,
                sessionMapper,
                messageArray,
                sseEmitter,
                sessionId,
                modeId,
                userId,
                questionId,
                topicId,
                problemId,
                messageArray.toJSONString(),
                session
        );

        // 构建WebSocket请求
        Request request = new Request.Builder()
                .url(listener.buildWebSocketUrl())
                .build();

        // 发起WebSocket连接
        WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        listener.setWebSocket(webSocket);
    }

    private String escapeContent(String content) {
        return content.replace("\\", "")
                .replace("\"", "")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}