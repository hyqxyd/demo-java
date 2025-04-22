package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class KimiService {
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private OkHttpClient okHttpClient;

    private final String API_URL = "https://api.moonshot.cn/v1/chat/completions";
    private final String API_KEY = "sk-ZW3wWtSxB7Cmskq8zasCqk2qEx2Qi15FMSqelzh0bDvK2JBK";
    private final String MODEL_NAME = "moonshot-v1-8k";
    private final int modeId = 4;

    public void callWithMessage(String prompt, SseEmitter sseEmitter) {
        // 解析输入参数
        JSONObject jsonObj = JSON.parseObject(prompt);
        String content = jsonObj.getString("content");
        int userId = jsonObj.getIntValue("id");
        String sessionId = jsonObj.getString("sessionId");
        int courseId = jsonObj.getIntValue("courseId");
        int topicId = jsonObj.getIntValue("topicId");
        int problemId = jsonObj.getIntValue("problemId");

        // 保存问题
        int questionId = questionService.saveQuestion(userId, courseId, content);

        // 会话管理
        Session session = sessionMapper.selectOne(
                new QueryWrapper<Session>().eq("id", sessionId).eq("user_id", userId));
        JSONArray messageArray = (session == null) ?
                new JSONArray() :
                JSON.parseArray(session.getContent());

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", escapeContent(content));
        messageArray.add(userMessage);

        // 构建API请求
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("temperature", 0.3);
        requestBody.put("stream", true);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().fluentPut("role", "system").fluentPut("content", "你是课程答疑机器人"));
        messages.addAll(messageArray);
        requestBody.put("messages", messages);

        RequestBody okHttpBody = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(okHttpBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
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

                while (!source.exhausted() && !isEnd) {
                    String line = source.readUtf8LineStrict();
                    if (line.startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (jsonData.isEmpty() || jsonData.equals("[DONE]")) continue;


                        JSONObject dataObj = JSON.parseObject(jsonData);
                        System.out.println(dataObj.toString());
                        JSONArray choices = dataObj.getJSONArray("choices");
                        JSONObject choice = choices.getJSONObject(0);

                        System.out.println(choice.toString());
                        String finishReason = choice.getString("finish_reason");
                        System.out.println(finishReason);
                        if ("stop".equals(finishReason)) {
                            System.out.println("finishReason");
                            isEnd = true;
                        }

                        System.out.println(isEnd);
                        JSONObject delta = choice.getJSONObject("delta");
                        String deltaContent = delta.getString("content");
                        if (deltaContent != null) {
                            System.out.println();
                            sseEmitter.send(SseEmitter.event()
                                    .data("{\"content\":\"" + escapeContent(deltaContent) + "\",\"is_end\":" + isEnd + "}"));
                            fullResponse.append(deltaContent);
                        }else {
                            sseEmitter.send(SseEmitter.event()
                                    .data("{\"content\":\"" + "" + "\",\"is_end\":" + isEnd + "}"));
                        }
                    }
                }

                // 保存完整响应
                String assistantResponse = fullResponse.toString();
                int answerId = answerService.saveAnswer(questionId, assistantResponse, modeId);

                // 更新会话历史
                JSONObject assistantMessage = new JSONObject();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", escapeContent(assistantResponse));
                messageArray.add(assistantMessage);

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

    private String escapeContent(String content) {
        return content.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}