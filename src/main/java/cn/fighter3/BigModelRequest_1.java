package cn.fighter3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BigModelRequest_1 extends WebSocketListener {
    private final String hostUrl = "https://spark-api.xf-yun.com/v3.5/chat";
    private final String apiKey = "4f103c4e097b970e48b2063dd7fd4234";
    private final String apiSecret = "NjUzOTNiOWM1NTU4NDlmYzkwMzg4YzAy";
    private final String appid = "aa7a0942";
    private WebSocket webSocket;
    private OkHttpClient client;
    private JSONArray text;
    private String data = "";
    private CompletableFuture<String> future;
    public CompletableFuture<String> getDataFuture() {
        return future;
    }

    public void setText(JSONArray text) {
        this.text = text;
    }
    public String getData() {
        return this.data;
    }
    public BigModelRequest_1(JSONArray text) throws Exception {
        this.text=text;
        client = new OkHttpClient();
        future = new CompletableFuture<>();
        try{
            // 构建鉴权URL
            String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
            // 替换为WebSocket协议
            String wsUrl = authUrl.replace("http://", "ws://").replace("https://", "wss://");
            Request request = new Request.Builder().url(wsUrl).build();
            // 创建WebSocket连接
            webSocket = client.newWebSocket(request, this);}catch (Exception e){
            e.printStackTrace();
            future.completeExceptionally(e);
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
        System.out.println("Content: " + content);

        this.data += content;
        if(choicesObject.getInt("status") == 2) {
            future.complete(data);
            Thread.sleep(200);
        }

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
        future.completeExceptionally(t);
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