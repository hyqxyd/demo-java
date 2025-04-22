package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import okhttp3.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class BigModelRequest extends WebSocketListener {
    private static final String HOST_URL = "https://spark-api.xf-yun.com/v3.5/chat";
    private static final String APP_ID = "aa7a0942";
    private static final String API_KEY = "4f103c4e097b970e48b2063dd7fd4234";
    private static final String API_SECRET = "NjUzOTNiOWM1NTU4NDlmYzkwMzg4YzAy";

    private final AnswerService answerService;
    private final SessionService sessionService;
    private final SessionMapper sessionMapper;
    private final JSONArray messageArray;
    private final SseEmitter sseEmitter;
    private final String sessionId;
    private final int modeId;
    private final int userId;
    private final int questionId;
    private final int topicId;
    private final int problemId;
    private final String originalMessages;
    private final Session existingSession;

    private WebSocket webSocket;
    private final StringBuilder fullResponse = new StringBuilder();

    public BigModelRequest(AnswerService answerService,
                           SessionService sessionService,
                           SessionMapper sessionMapper,
                           JSONArray messageArray,
                           SseEmitter sseEmitter,
                           String sessionId,
                           int modeId,
                           int userId,
                           int questionId,
                           int topicId,
                           int problemId,
                           String originalMessages,
                           Session existingSession) {
        this.answerService = answerService;
        this.sessionService = sessionService;
        this.sessionMapper = sessionMapper;
        this.messageArray = messageArray;
        this.sseEmitter = sseEmitter;
        this.sessionId = sessionId;
        this.modeId = modeId;
        this.userId = userId;
        this.questionId = questionId;
        this.topicId = topicId;
        this.problemId = problemId;
        this.originalMessages = originalMessages;
        this.existingSession = existingSession;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        JSONObject requestJson = buildRequest();
        webSocket.send(requestJson.toJSONString());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JSONObject responseObj = JSONObject.parseObject(text);
            JSONObject payload = responseObj.getJSONObject("payload");
            JSONObject choices = payload.getJSONObject("choices");
            JSONObject textObj = choices.getJSONArray("text").getJSONObject(0);
            String deltaContent = textObj.getString("content");
            boolean isEnd = choices.getIntValue("status") == 2;

            sendSSEEvent(deltaContent, isEnd);
            fullResponse.append(deltaContent);

            if (isEnd) {
                saveAnswerAndUpdateSession();
                sseEmitter.complete();
            }
        } catch (Exception e) {
            sseEmitter.completeWithError(e);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        sseEmitter.completeWithError(t);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        System.out.println("WebSocket连接已关闭");
    }

    private JSONObject buildRequest() {
        JSONObject header = new JSONObject();
        header.put("app_id", APP_ID);
        header.put("uid", UUID.randomUUID().toString().substring(0, 10));

        JSONObject parameter = new JSONObject();
        JSONObject chat = new JSONObject();
        chat.put("domain", "generalv3.5");
        chat.put("temperature", 0.5);
        chat.put("max_tokens", 4096);
        parameter.put("chat", chat);

        JSONObject payload = new JSONObject();
        JSONObject message = new JSONObject();
        message.put("text", messageArray);
        payload.put("message", message);

        JSONObject requestJson = new JSONObject();
        requestJson.put("header", header);
        requestJson.put("parameter", parameter);
        requestJson.put("payload", payload);
        return requestJson;
    }

    public String buildWebSocketUrl() throws Exception {
        URL url = new URL(HOST_URL);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";

        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(API_SECRET.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        String sha = Base64.getEncoder().encodeToString(mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8)));

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                API_KEY, "hmac-sha256", "host date request-line", sha);

        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();

        return httpUrl.toString().replace("https://", "wss://");
    }

    private void sendSSEEvent(String content, boolean isEnd) throws IOException {
        String escapedContent = content.replace("\\", "")
                .replace("\"", "")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        sseEmitter.send(SseEmitter.event()
                .data("{\"content\":\"" + escapedContent + "\",\"is_end\":" + isEnd + "}"));
    }

    private void saveAnswerAndUpdateSession() {
        String assistantResponse = fullResponse.toString();
        int answerId = answerService.saveAnswer(questionId, assistantResponse, modeId);

        JSONObject assistantMessage = new JSONObject();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", escapeContent(assistantResponse));
        messageArray.add(assistantMessage);

        if (existingSession == null) {
            sessionService.saveSession(sessionId, questionId, answerId, modeId, topicId, userId,
                    messageArray.toJSONString(),problemId);
        } else {
            UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", sessionId).eq("user_id", userId);
            existingSession.setContent(messageArray.toJSONString());
            sessionMapper.update(existingSession, updateWrapper);
        }
    }

    private String escapeContent(String content) {
        return content.replace("\\", "")
                .replace("\"", "")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}