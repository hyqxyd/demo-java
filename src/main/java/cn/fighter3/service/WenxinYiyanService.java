package cn.fighter3.service;

import cn.fighter3.config.AppConfig;
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
public class WenxinYiyanService {
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
    @Autowired
    private AppConfig config;

    private final int modeId = 1;
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String API_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k";
    private static final String API_KEY = "your_api_key";
    private static final String SECRET_KEY = "your_secret_key";

    public void callWithMessage(String prompt, SseEmitter sseEmitter) {
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

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", escapeContent(content));
        messageArray.add(userMessage);

        // 获取access_token
        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            sseEmitter.completeWithError(e);
            return;
        }

        // 构造API请求
        JSONObject requestBody = new JSONObject();
        requestBody.put("messages", messageArray);
        requestBody.put("stream", true);

        RequestBody okHttpBody = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL + "?access_token=" + accessToken)
                .post(okHttpBody)
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
                    String line = source.readUtf8Line();
                    if (line == null || line.trim().isEmpty()) continue;

                    try {
                        JSONObject data = JSON.parseObject("{" + line + "}");
                        JSONObject dataObj = data.getJSONObject("data");
                        String result = dataObj.getString("result");
                        isEnd = dataObj.getBoolean("is_end");

                        sseEmitter.send(SseEmitter.event()
                                .data("{\"content\":\"" + escapeContent(result) + "\",\"is_end\":" + isEnd + "}"));
                        fullResponse.append(result);
                    } catch (Exception e) {
                        sseEmitter.completeWithError(e);
                    }
                }

                // 保存完整响应
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

    private String getAccessToken() throws Exception {
        RequestBody body = RequestBody.create(
                "grant_type=client_credentials&client_id=" + config.getApiKey() + "&client_secret=" + config.getSecretKey(),
                MediaType.parse("application/x-www-form-urlencoded")
        );

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            JSONObject json = JSON.parseObject(response.body().string());
            return json.getString("access_token");
        }
    }

    /**
     * 获取问题的答案（同步方法，用于RAG服务）
     */
    public String getAnswer(String prompt, int userId, String sessionId) throws IOException {
        // 获取access_token
        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            throw new IOException("获取访问令牌失败: " + e.getMessage(), e);
        }

        // 构造消息数组
        JSONArray messageArray = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messageArray.add(userMessage);

        // 构造API请求
        JSONObject requestBody = new JSONObject();
        requestBody.put("messages", messageArray);
        requestBody.put("stream", false); // 非流式响应

        RequestBody okHttpBody = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL + "?access_token=" + accessToken)
                .post(okHttpBody)
                .addHeader("Content-Type", "application/json")
                .build();

        // 执行请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API请求失败: " + response.code() + " " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("响应体为空");
            }

            String responseStr = body.string();
            JSONObject responseJson = JSON.parseObject(responseStr);
            
            // 检查是否有错误
            if (responseJson.containsKey("error_code")) {
                throw new IOException("API返回错误: " + responseJson.getString("error_msg"));
            }

            // 提取结果
            return responseJson.getString("result");
        }
    }

    private String escapeContent(String content) {
        return content.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}