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
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.AbstractMap;

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

    
    // 获取维度权重
    private double getDimensionWeight(String dimension) {
        Map<String, Double> weights = new HashMap<>();
        weights.put("remember", 0.15);
        weights.put("understand", 0.20);
        weights.put("apply", 0.25);
        weights.put("analyze", 0.20);
        weights.put("evaluate", 0.12);
        weights.put("create", 0.08);
        
        return weights.getOrDefault(dimension, 0.0);
    }
    
    // 创建各维度的提示词
    private Map<String, String> createDimensionPrompts() {
        Map<String, String> prompts = new HashMap<>();
        
        prompts.put("remember", "请分析对话中用户提出的问题，判断有多少属于'记忆'认知层次（涉及回忆、识别、记住信息等）。" +
                "请计算：用户总共提出了多少个问题(n)，其中多少个问题(m)属于记忆认知层次，然后计算m/n*100得到百分比分数。" +
                "同时，请提供简短分析，包含2个问题中的文本证据和1个理论依据。" +
                "返回格式：{\"score\": 分数, \"analysis\": \"分析内容\"}");
        
        prompts.put("understand", "请分析对话中用户提出的问题，判断有多少属于'理解'认知层次（涉及解释、总结、举例、分类等）。" +
                "请计算：用户总共提出了多少个问题(n)，其中多少个问题(m)属于理解认知层次，然后计算m/n*100得到百分比分数。" +
                "同时，请提供简短分析，包含2个问题中的文本证据和1个理论依据。" +
                "返回格式：{\"score\": 分数, \"analysis\": \"分析内容\"}");
        
        prompts.put("apply", "请分析对话中用户提出的问题，判断有多少属于'应用'认知层次（涉及执行、实施、使用知识解决问题等）。" +
                "请计算：用户总共提出了多少个问题(n)，其中多少个问题(m)属于应用认知层次，然后计算m/n*100得到百分比分数。" +
                "同时，请提供简短分析，包含2个问题中的文本证据和1个理论依据。" +
                "返回格式：{\"score\": 分数, \"analysis\": \"分析内容\"}");
        
        prompts.put("analyze", "请分析对话中用户提出的问题，判断有多少属于'分析'认知层次（涉及区分、组织、归因等）。" +
                "请计算：用户总共提出了多少个问题(n)，其中多少个问题(m)属于分析认知层次，然后计算m/n*100得到百分比分数。" +
                "同时，请提供简短分析，包含2个问题中的文本证据和1个理论依据。" +
                "返回格式：{\"score\": 分数, \"analysis\": \"分析内容\"}");
        
        prompts.put("evaluate", "请分析对话中用户提出的问题，判断有多少属于'评价'认知层次（涉及检查、批判、判断等）。" +
                "请计算：用户总共提出了多少个问题(n)，其中多少个问题(m)属于评价认知层次，然后计算m/n*100得到百分比分数。" +
                "同时，请提供简短分析，包含2个问题中的文本证据和1个理论依据。" +
                "返回格式：{\"score\": 分数, \"analysis\": \"分析内容\"}");
        
        prompts.put("create", "请分析对话中用户提出的问题，判断有多少属于'创造'认知层次（涉及产生、计划、创作等）。" +
                "请计算：用户总共提出了多少个问题(n)，其中多少个问题(m)属于创造认知层次，然后计算m/n*100得到百分比分数。" +
                "同时，请提供简短分析，包含2个问题中的文本证据和1个理论依据。" +
                "返回格式：{\"score\": 分数, \"analysis\": \"分析内容\"}");
        
        return prompts;
    }
    
    // 处理单个认知维度
    private Map<String, Object> processDimension(String interaction, String dimensionPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-v3");
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加system消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个教育评估专家，擅长分析对话中的认知层次。请严格按照要求的JSON格式返回结果。");
        messages.add(systemMessage);
        
        // 添加user消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", dimensionPrompt + "\n\n对话记录：\n" + interaction);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1024);
        requestBody.put("temperature", 0.2);
        requestBody.put("stream", false);
        
        try {
            String response = streamToDeepSeek(requestBody);
            if (response == null || response.trim().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("score", 0.0);
                result.put("analysis", "无法获取分析结果");
                return result;
            }
            
            return parseDimensionResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("score", 0.0);
            result.put("analysis", "处理分析时出错: " + e.getMessage());
            return result;
        }
    }
    
    // 从响应中解析维度评估结果
    private Map<String, Object> parseDimensionResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查响应是否为空
            if (response == null || response.trim().isEmpty()) {
                result.put("score", 0.0);
                result.put("analysis", "响应为空");
                return result;
            }
            
            JSONObject json = new JSONObject(response);
            String content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            
            // 尝试解析JSON格式的内容
            try {
                // 提取JSON部分
                int startIndex = content.indexOf("{");
                int endIndex = content.lastIndexOf("}") + 1;
                
                if (startIndex >= 0 && endIndex > startIndex) {
                    String jsonContent = content.substring(startIndex, endIndex);
                    JSONObject contentJson = new JSONObject(jsonContent);
                    
                    if (contentJson.has("score")) {
                        result.put("score", contentJson.getDouble("score"));
                    } else {
                        // 如果没有找到score字段，尝试从文本中提取
                        result.put("score", extractScoreFromText(content));
                    }
                    
                    if (contentJson.has("analysis")) {
                        result.put("analysis", contentJson.getString("analysis"));
                    } else {
                        // 如果没有找到analysis字段，使用整个内容作为分析
                        result.put("analysis", content);
                    }
                    
                    return result;
                }
            } catch (Exception e) {
                // JSON解析失败，使用备用方法
                System.err.println("JSON解析失败，使用备用方法: " + e.getMessage());
            }
            
            // 备用方法：直接从文本中提取分数和分析
            double score = extractScoreFromText(content);
            result.put("score", score);
            result.put("analysis", content);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("score", 0.0);
            result.put("analysis", "解析响应时出错: " + e.getMessage());
            return result;
        }
    }
    
    // 从文本中提取分数
    private double extractScoreFromText(String content) {
        try {
            // 提取比值并转换为百分比
            Pattern pattern = Pattern.compile("(\\d+)\\s*\\/\\s*(\\d+)");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                double m = Double.parseDouble(matcher.group(1));
                double n = Double.parseDouble(matcher.group(2));
                
                if (n > 0) {
                    return Math.round((m / n) * 100);
                }
            }
            
            // 尝试直接提取百分比
            pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*%");
            matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
            
            // 尝试提取分数
            pattern = Pattern.compile("分数[：:]*\\s*(\\d+(\\.\\d+)?)");
            matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
            
            // 如果以上方法都失败，尝试提取任何数字
            pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
            matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                double value = Double.parseDouble(matcher.group(1));
                // 如果值大于1且小于等于100，假设它是百分比
                if (value > 1 && value <= 100) {
                    return value;
                } else if (value <= 1) {
                    // 如果值小于等于1，假设它是比例
                    return value * 100;
                }
            }
            
            return 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
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
        if (interaction == null || interaction.trim().isEmpty()) {
            return "{\"error\": \"对话内容为空\"}";
        }

        // 并行处理六个认知维度
        Map<String, Object> finalResult = new HashMap<>();
        Map<String, Object> dimensionScores = new HashMap<>();
        StringBuilder analysisBuilder = new StringBuilder();
        
        // 创建并发任务列表
        List<CompletableFuture<Map.Entry<String, Map<String, Object>>>> futures = new ArrayList<>();
        
        // 并行处理六个认知维度
        try {
            // 创建并发任务
            Map<String, String> dimensionPrompts = createDimensionPrompts();
            
            for (Map.Entry<String, String> entry : dimensionPrompts.entrySet()) {
                String dimension = entry.getKey();
                String prompt = entry.getValue();
                
                CompletableFuture<Map.Entry<String, Map<String, Object>>> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Map<String, Object> result = processDimension(interaction, prompt);
                        return new AbstractMap.SimpleEntry<>(dimension, result);
                    } catch (Exception e) {
                        System.err.println("处理维度 " + dimension + " 时出错: " + e.getMessage());
                        e.printStackTrace();
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("score", 0.0);
                        errorResult.put("analysis", "处理时出错: " + e.getMessage());
                        return new AbstractMap.SimpleEntry<>(dimension, errorResult);
                    }
                });
                
                futures.add(future);
            }
            
            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            
            // 获取所有结果
            allFutures.join();
            
            // 整合结果
            double totalScore = 0;
            
            for (CompletableFuture<Map.Entry<String, Map<String, Object>>> future : futures) {
                try {
                    Map.Entry<String, Map<String, Object>> result = future.get();
                    String dimension = result.getKey();
                    Map<String, Object> dimensionResult = result.getValue();
                    
                    double score = ((Number) dimensionResult.get("score")).doubleValue();
                    String analysis = (String) dimensionResult.get("analysis");
                    
                    // 添加到维度分数
                    dimensionScores.put(dimension, score);
                    
                    // 根据权重计算总分
                    double weight = getDimensionWeight(dimension);
                    totalScore += score * weight;
                    
                    // 添加到分析报告
                    String chineseName = getDimensionChineseName(dimension);
                    analysisBuilder.append(chineseName).append(" ").append(score).append("分：").append(analysis).append("\n\n");
                } catch (Exception e) {
                    System.err.println("获取结果时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 构建最终结果
            finalResult.put("dimension_scores", dimensionScores);
            finalResult.put("score", Math.round(totalScore * 10) / 10.0); // 保留一位小数
            finalResult.put("analysis", analysisBuilder.toString());
            
            return JSON.toJSONString(finalResult);
            
        } catch (Exception e) {
            System.err.println("处理会话时出错: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"处理认知维度时出错: " + e.getMessage() + "\"}";
        }
    }
    
    // 获取维度的中文名称
    private String getDimensionChineseName(String dimension) {
        Map<String, String> names = new HashMap<>();
        names.put("remember", "记忆");
        names.put("understand", "理解");
        names.put("apply", "应用");
        names.put("analyze", "分析");
        names.put("evaluate", "评价");
        names.put("create", "创造");
        
        return names.getOrDefault(dimension, dimension);
    }

    // 新增方法：构建评估prompt（保留原方法，但不再使用）
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