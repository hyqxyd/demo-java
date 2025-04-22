package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TongyiQianwenService {
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
    private final int modeId = 2;

    public void callWithMessage(String prompt, SseEmitter sseEmitter)
            throws ApiException, NoApiKeyException, InputRequiredException {

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

        // 构造API请求
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-plus");

        JSONObject input = new JSONObject();
        input.put("messages", messageArray);
        requestBody.put("input", input);

        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        parameters.put("incremental_output", true);
        requestBody.put("parameters", parameters);
        requestBody.put("stream", true);

        // 发送请求
        RequestBody okHttpBody = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                .post(okHttpBody)
                .addHeader("Authorization", "Bearer " + "sk-6a0845249f1d49b5bd2420e78597a523")
                .addHeader("X-DashScope-SSE", "enable")
                .build();

        // 异步处理响应
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    sseEmitter.completeWithError(new RuntimeException("API请求失败: " + response.code()));
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    sseEmitter.complete();
                    return;
                }

                BufferedSource source = body.source();
                StringBuilder fullResponse = new StringBuilder();
                boolean isEnd = false;

                // 处理流式响应
                while (!source.exhausted() && !isEnd) {
                    String line = source.readUtf8LineStrict();
                    if (line.startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (jsonData.isEmpty() || jsonData.equals("[DONE]")) continue;

                        JSONObject dataObj = JSON.parseObject(jsonData);
                        JSONObject output = dataObj.getJSONObject("output");
                        JSONArray choices = output.getJSONArray("choices");
                        JSONObject choice = choices.getJSONObject(0);
                        String finishReason = choice.getString("finish_reason");

                        if ("stop".equals(finishReason)) {
                            isEnd = true;
                        }

                        JSONObject message = choice.getJSONObject("message");
                        String deltaContent = message.getString("content");
                        System.out.println("deltaContent: " + deltaContent);
                        sseEmitter.send(SseEmitter.event()
                                .data("{\"content\":\"" + escapeContent(deltaContent) + "\",\"is_end\":" + isEnd + "}"));
                        fullResponse.append(deltaContent);
                    }
                }

                // 保存完整响应和会话
                String assistantResponse = fullResponse.toString();
                int answerId = answerService.saveAnswer(questionId, assistantResponse, modeId);

                // 添加助手消息
                JSONObject assistantMessage = new JSONObject();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", escapeContent(assistantResponse));
                messageArray.add(assistantMessage);

                // 更新会话
                if (session == null) {
                    sessionService.saveSession(sessionId, questionId, answerId, modeId, topicId, userId, messageArray.toJSONString(),problemId);
                } else {
                    session.setContent(messageArray.toJSONString());
                    sessionMapper.updateById(session);
                }

                sseEmitter.complete();
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        });
    }

    // 转义JSON特殊字符
    private String escapeContent(String content) {
        return content.replace("\\", "")
                .replace("\"", "")
                // .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}