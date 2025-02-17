package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import cn.fighter3.service.SessionService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BigModelRequest extends WebSocketListener {
    private final String hostUrl = "https://spark-api.xf-yun.com/v3.5/chat";
    private final String apiKey = "4f103c4e097b970e48b2063dd7fd4234";
    private final String apiSecret = "NjUzOTNiOWM1NTU4NDlmYzkwMzg4YzAy";
    private final String appid = "aa7a0942";
    private WebSocket webSocket;
    private OkHttpClient client;
    private JSONArray text;
    private String data = "";
    private SseEmitter sseEmitter;

    private AnswerService answerService;
    private String s_id;
    private int modeId;
    private int user_id;
    private int q_id;
    private String messages;
    private  SessionService sessionService;
    private SessionMapper sessionMapper;
    private Session session;
    private int flag;//是否为历史对话
    public void setText(JSONArray text ) {
        this.text = text;
    }
    public String getData() {
        return this.data;
    }
    public BigModelRequest(AnswerService answerservice,SessionService sessionService,SessionMapper sessionMapper,JSONArray text,SseEmitter sseEmitter,String s_id,int modeId ,int user_id,int q_id,String messages,Session session,int flag) throws Exception {
        this.text=text;
        this.sseEmitter=sseEmitter;
        this.s_id=s_id;
        this.modeId=modeId;
        this.user_id=user_id;
        this.q_id=q_id;
        this.messages=messages;
        this.session=session;
        this.flag=flag;
        this.answerService=answerservice;
        this.sessionService=sessionService;
        this.sessionMapper=sessionMapper;
        client = new OkHttpClient();

        try{
            // 构建鉴权URL
            String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
            // 替换为WebSocket协议
            String wsUrl = authUrl.replace("http://", "ws://").replace("https://", "wss://");
            Request request = new Request.Builder().url(wsUrl).build();
            // 创建WebSocket连接
            webSocket = client.newWebSocket(request, this);}catch (Exception e){
            e.printStackTrace();

        }
    }

    @Override
    public void onOpen(WebSocket webSocket, okhttp3.Response response) {
        super.onOpen(webSocket, response);
        System.out.println("WebSocket连接已打开");
        // 构建请求体
        buildAndSendRequest();
    }

    private void buildAndSendRequest() {
        // Header部分
        JSONObject header = new JSONObject();
        header.put("app_id", appid);
        header.put("uid", UUID.randomUUID().toString().substring(0, 10));

        // Parameter部分
        JSONObject parameter = new JSONObject();
        JSONObject chat = new JSONObject();
        chat.put("domain", "generalv3.5");
        chat.put("temperature", 0.5);
        chat.put("max_tokens", 4096);
        parameter.put("chat", chat);

        // Payload部分
        JSONObject payload = new JSONObject();
        JSONObject message = new JSONObject();

        message.put("text", this.text);
        payload.put("message", message);

        // 完整的请求体
        JSONObject requestJson = new JSONObject();
        requestJson.put("header", header);
        requestJson.put("parameter", parameter);
        requestJson.put("payload", payload);

        // 打印请求体
        System.out.println("发送请求体：" + requestJson.toJSONString());
        // 发送请求
        webSocket.send(requestJson.toJSONString());
    }

    @SneakyThrows
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        boolean isend=false;
        System.out.println("收到服务器消息：" + text);

        System.out.println("接收到的消息: " + text);
//                // 这里可以处理接收到的消息
        org.json.JSONObject jsonObject = new org.json.JSONObject( text );
        System.out.println(jsonObject);
        String content = jsonObject.getJSONObject("payload").toString();
        System.out.println(content);
        jsonObject = new org.json.JSONObject(content);
        org.json.JSONObject choicesObject = jsonObject.getJSONObject("choices");
        System.out.println(choicesObject.toString());
        System.out.println(choicesObject.getInt("status"));

        org.json.JSONObject deltaObject = choicesObject.getJSONArray("text").getJSONObject(0);
        System.out.println(deltaObject.toString());
        content = deltaObject.getString("content");
        content=content.replace("\n","\\n");
        System.out.println("Content: " + content);
        this.data += content;
        if(choicesObject.getInt("status") == 2) {
            isend=true;
            System.out.println("获取到的回答："+data);
            int a_id=answerService.saveAnswer(q_id,data,modeId);
            System.out.println("答案保存成功！");
            messages+=","+"{\"role\":\"assistant\",\"content\":\"" + data +"\"}";
            if(flag==1) {
                sessionService.saveSession(s_id, q_id, a_id, modeId, 1, user_id,messages);
            }else {
                UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", s_id).eq("user_id", user_id);

                Session newsession = session;
                newsession.setContent(messages);
                newsession.setSessionTime();

                sessionMapper.update(newsession, updateWrapper);

            }

        }
        final String finalContent = content;
        final boolean finalIsend = isend;
        new Thread(() -> {
            try {
                sseEmitter.send(SseEmitter.event().data("{\"content\":\"" + finalContent + "\",\"is_end\":\"" + finalIsend + "\"}"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();


    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        System.out.println("WebSocket连接已关闭");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
        super.onFailure(webSocket, t, response);
        System.out.println("WebSocket连接失败：" + t.getMessage());

    }

    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        // 实现鉴权逻辑，返回鉴权后的URL
        // 可以参考之前的getAuthUrl方法实现
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }


}