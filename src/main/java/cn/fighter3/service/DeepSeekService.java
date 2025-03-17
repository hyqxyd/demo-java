package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import cn.fighter3.mapper.ModelMapper;
import cn.fighter3.entity.Model;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSON;

@Service
public class DeepSeekService {
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private ModelMapper modelMapper;
    // 修改后的流式处理方法
    private String streamToDeepSeek(Map<String, Object> requestBody) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        try {
            Request request = new Request.Builder()
                    .url("https://api.deepseek.com/chat/completions")
                    .post(RequestBody.create(mediaType, JSON.toJSONString(requestBody)))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept","application/json")
                    .addHeader("Authorization", "Bearer sk-7e5617f5f6d24d85a589559a34bb6287")
                    .build();

            StringBuilder fullResponse = new StringBuilder();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return null;
                }
                System.out.println(response.toString());

                BufferedSource source = response.body().source();
                while (!source.exhausted()) {
                    String line = source.readUtf8Line();
                    if (line != null && line.startsWith("data:")) {
                        String jsonContent = line.substring(5).trim();
                        if (!jsonContent.isEmpty()) {
                            String content = parseResponse(jsonContent);
                            fullResponse.append(content);
                        }
                    }
                }
            }
            return fullResponse.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 新增方法：获取用户会话列表
    public List<Session> getUserSessions(int userId) {
        QueryWrapper<Session> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .orderByDesc("session_time");
        return sessionMapper.selectList(query);
    }
    // 新增方法：解析对话内容
    private String parseInteraction(String content) {
        try {
            return "[\n" + content + "\n]";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return "解析互动错误";
        }
    }
    // 修改后的处理方法（增加sessionId参数）
    public String processSession(int userId, String sessionId) {
        // 联合验证用户和会话的归属关系
        QueryWrapper<Session> query = new QueryWrapper<>();
        query.eq("id", sessionId)
                .eq("user_id", userId);

        Session session = sessionMapper.selectOne(query);
        if (session == null) return null;

        // 获取模型名称
        Model model = modelMapper.selectById(session.getMId());
        String modelName = (model != null) ? model.getModelName() : "未知模型";

        // 构建对话记录
        String interaction = parseInteraction(session.getContent());

        // 构建完整请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");

        // 构建messages列表（包含system和user消息）
        List<Map<String, String>> messages = new ArrayList<>();
        // 添加system消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant");
        messages.add(systemMessage);
        // 添加user消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", buildPrompt(interaction));
        messages.add(userMessage);
        requestBody.put("messages", messages);
        // 添加其他参数
        requestBody.put("frequency_penalty", 0);
        requestBody.put("max_tokens", 2048);
        requestBody.put("presence_penalty", 0);
        // 构建response_format对象
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "text");//待定 json_object
        requestBody.put("response_format", responseFormat);
        requestBody.put("stop", null);
        requestBody.put("stream", true);
        requestBody.put("stream_options", null);
        requestBody.put("temperature", 1.0);
        requestBody.put("top_p", 1.0);
        requestBody.put("tools", null);
        requestBody.put("tool_choice", "none");
        requestBody.put("logprobs", false);
        requestBody.put("top_logprobs", null);

        return streamToDeepSeek(requestBody);
    }



    // 新增方法：构建评估prompt
    private String buildPrompt(String interaction) {
        return "你是一个教育专家，请基于Bloom认知模型分析以下对话（0-100分），评估维度：\n"
                + "1. 知识理解深度（40%）\n2. 复杂应用能力（30%）\n3. 批判性思维（20%）\n4. 元认知（10%）\n\n"
                + "返回JSON格式：{\"score\": 分数, \"analysis\": \"分析内容\"}\n\n对话记录：\n"
                + interaction;
    }
    // 新增流式响应解析方法
    private String parseResponse(String chunk) {
        try {
            JSONObject json = new JSONObject(chunk);
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("delta")
                    .optString("content", "");
        } catch (Exception e) {
            return "";
        }


    }



}