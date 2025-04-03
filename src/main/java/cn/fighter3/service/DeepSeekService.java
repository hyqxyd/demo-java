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
                    .url("https://maas-api.ai-yuanjing.com/openapi/compatible-mode/v1/chat/completions")
                    .post(RequestBody.create(mediaType, JSON.toJSONString(requestBody)))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept","application/json")
                    .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImIzODRmZDhhLWNmZjUtNDY2Yi04MTliLWVlZWNiNDM3NmIyMyIsInVzZXJuYW1lIjoieGlseTExIiwibmlja25hbWUiOiLluK3npLzmtIsiLCJ1c2VyVHlwZSI6MCwiYnVmZmVyVGltZSI6MTc0MjIxNTIxNywiZXhwIjoxNzQ0ODAwMDE3LCJqdGkiOiI2ZmJjM2Q1ZTJkMzg0NWE3YTNhNGQyOGRhZmNkNjBmYSIsImlhdCI6MTc0MjIwNzg5NywiaXNzIjoiYjM4NGZkOGEtY2ZmNS00NjZiLTgxOWItZWVlY2I0Mzc2YjIzIiwibmJmIjoxNzQyMjA3ODk3LCJzdWIiOiJrb25nIn0.7c4_cGSd9muxHExDq-hdE0c3oqAJJ-zBZFLD_TpXkVQ")
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
                            System.out.println(content);
                            fullResponse.append(content);
                        }
                    }
                }
            }
            System.out.println(fullResponse.toString());
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
        requestBody.put("model", "deepseek-v3");

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
        // 构建response_format对象
        requestBody.put("stream", true);
        requestBody.put("top_p", 1.0);

        return streamToDeepSeek(requestBody);
    }



    // 新增方法：构建评估prompt
    private String buildPrompt(String interaction) {
        return "作为教育评估专家，请基于修订版Bloom认知分类法(Krathwohl, 2002)和问题分类理论(Nirenburg et al., 2023)，对用户提出的问题进行认知层次分析。\n\n"
                + "# 评估标准（依据ACM SIGCSE 2023）\n"
                + "1. 认知维度评分（0-100）：\n"
                + "   - 记忆(15%)：术语准确性（Mayer认知负荷理论,2019）\n"
                + "   - 理解(20%)：概念转化（Chi自我解释理论,2020）\n"
                + "   - 应用(25%)：情境迁移（Koedinger模型,2022）\n"
                + "   - 分析(20%)：逻辑解构（Graesser框架,2021）\n"
                + "   - 评价(12%)：论证质量（Toulmin模型,2023）\n"
                + "   - 创造(8%)：创新等级（Runco指数,2023）\n\n"
                + "2. 分析要求：\n"
                + "   - 每个维度必须包含：\n"
                + "     a) 2个问题中的文本证据\n"
                + "     b) 1个理论依据引用\n"
                + "   - 创新评分需满足：跨领域/专利要素/创新指数≥0.7\n\n"
                + "3. 返回JSON格式：\n"
                + "{\n"
                + "   \"dimension_scores\": {\n"
                + "       \"remember\": 记忆分数,\n"
                + "       \"understand\": 理解分数,\n"
                + "       \"apply\": 应用分数,\n"
                + "       \"analyze\": 分析分数,\n"
                + "       \"evaluate\": 评价分数,\n"
                + "       \"create\": 创造分数\n"
                + "   },\n"
                + "   \"score\": 总分,\n" // 计算验证：(85*0.15)+(90*0.2)+(78*0.25)+(82*0.2)+(88*0.12)+(75*0.08)
                + "   \"analysis\": \"逐项分析各维度表现，突出优势与改进建议\"\n"
                + "#analysis 示例分析片段：\n"
                + "\"记忆 85分：问题中精确使用'时间复杂度'术语（证据1），正确引用'10万条数据耗时3秒'（证据2），符合Mayer认知负荷理论的精确性要求（理论依据）。\n"

                + "}\n\n"
                + "对话记录：\n"
                + interaction
                + "\n\n请根据上述评估标准，对对话记录中role==user的content进行认知层次分析，不要对role==assistant的content进行分析，我需要的是用户提出的问题进行分析并返回JSON格式的评估结果。";
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