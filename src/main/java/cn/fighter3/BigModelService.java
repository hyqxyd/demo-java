package cn.fighter3;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BigModelService {
    private static final String hostUrl = "https://spark-api.xf-yun.com/v3.5/chat";
    private static final String domain = "generalv3.5";
    private static final String appid = "aa7a0942"; // 请替换为您的appid
    private static final String apiSecret = "NjUzOTNiOWM1NTU4NDlmYzkwMzg4YzAy"; // 请替换为您的apiSecret
    private static final String apiKey = "4f103c4e097b970e48b2063dd7fd4234"; // 请替换为您的apiKey

    /**
     * 发送请求到讯飞星火认知大模型API，并返回AI的回答
     *
     * @param userId 用户ID
     * @param historyList 历史对话列表
     * @param newQuestion 最新问题
     * @return AI的回答
     * @throws Exception 可能抛出的异常
     */
    public String sendQuestionAndGetAnswer(String userId, List<RoleContent> historyList, String newQuestion) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient();
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();

        // 创建WebSocket监听器
        BigModelNew listener = new BigModelNew(userId, historyList, newQuestion, latch);
        WebSocket webSocket = client.newWebSocket(request, listener);

        // 等待WebSocket关闭或超时
        latch.await(30, TimeUnit.SECONDS);
        return listener.getAnswer();
    }

    /**
     * 构建鉴权URL
     */
    private static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        // 实现鉴权URL的构建逻辑
        // 这里需要根据讯飞API的要求来构建鉴权URL
        // ...
        return "构建的鉴权URL"; // 返回构建的鉴权URL
    }

    /**
     * WebSocket监听器，处理WebSocket连接、消息和关闭事件
     */
    public static class BigModelNew extends WebSocketListener {
        private final String userId;
        private final List<RoleContent> historyList;
        private final String newQuestion;
        private final CountDownLatch latch;
        private String totalAnswer;
        private volatile boolean wsCloseFlag;

        public BigModelNew(String userId, List<RoleContent> historyList, String newQuestion, CountDownLatch latch) {
            this.userId = userId;
            this.historyList = historyList;
            this.newQuestion = newQuestion;
            this.latch = latch;
            this.totalAnswer = "";
            this.wsCloseFlag = false;
        }

        public String getAnswer() {
            return totalAnswer;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            // 发送请求到讯飞星火认知大模型API
            sendRequest(webSocket);
        }

        private void sendRequest(WebSocket webSocket) {
            // 构建请求JSON
            JSONObject requestJson = buildRequestJson();
            webSocket.send(requestJson.toJSONString());
        }

        private JSONObject buildRequestJson() {
            // 构建请求JSON的逻辑
            // ...
            return new JSONObject();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            // 处理从讯飞星火认知大模型API接收到的消息
            // ...
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            // WebSocket关闭时设置标志
            wsCloseFlag = true;
            totalAnswer = "Session closed: " + reason;
            latch.countDown();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            // 处理WebSocket连接失败
            t.printStackTrace();
            totalAnswer = "Connection failed: " + t.getMessage();
            latch.countDown();
        }
    }

    /**
     * 角色内容类，用于存储对话历史和问题
     */
    public static class RoleContent {
        private String role;
        private String content;

        public RoleContent(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}