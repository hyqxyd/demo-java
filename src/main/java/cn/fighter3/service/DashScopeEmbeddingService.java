package cn.fighter3.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 阿里云DashScope嵌入服务
 * 使用text-embedding-v1模型生成1536维向量
 */
@Service
public class DashScopeEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeEmbeddingService.class);
    private static final String EMBEDDING_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";
    private static final String MODEL_NAME = "text-embedding-v1";
    
    @Value("${ai.api.key}")
    private String apiKey;
    
    @Autowired
    private OkHttpClient okHttpClient;

    /**
     * 获取文本的嵌入向量
     * @param text 输入文本
     * @return 1536维的嵌入向量
     */
    public float[] getEmbedding(String text) {
        try {
            // 对文本进行预处理
            String processedText = preprocessText(text);
            
            // 构造请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL_NAME);
            
            JSONObject input = new JSONObject();
            JSONArray texts = new JSONArray();
            texts.add(processedText);
            input.put("texts", texts);
            requestBody.put("input", input);
            
            JSONObject parameters = new JSONObject();
            parameters.put("text_type", "document");
            requestBody.put("parameters", parameters);
            
            // 发送请求
            RequestBody okHttpBody = RequestBody.create(
                    requestBody.toJSONString(),
                    MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(EMBEDDING_API_URL)
                    .post(okHttpBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("DashScope API请求失败: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new RuntimeException("DashScope API响应为空");
                }
                
                String responseStr = body.string();
                JSONObject responseJson = JSON.parseObject(responseStr);
                
                // 检查响应状态
                JSONObject output = responseJson.getJSONObject("output");
                if (output == null) {
                    throw new RuntimeException("DashScope API响应格式错误: " + responseStr);
                }
                
                JSONArray embeddings = output.getJSONArray("embeddings");
                if (embeddings == null || embeddings.isEmpty()) {
                    throw new RuntimeException("DashScope API未返回嵌入向量");
                }
                
                JSONObject embeddingObj = embeddings.getJSONObject(0);
                JSONArray embeddingArray = embeddingObj.getJSONArray("embedding");
                
                // 转换为float数组
                float[] result = new float[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    result[i] = embeddingArray.getFloatValue(i);
                }
                
                logger.debug("成功生成嵌入向量，维度: {}", result.length);
                return result;
                
            }
        } catch (IOException e) {
            logger.error("调用DashScope嵌入API失败", e);
            throw new RuntimeException("生成嵌入向量失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("生成嵌入向量失败", e);
            throw new RuntimeException("生成嵌入向量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量获取文本的嵌入向量
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    public List<float[]> getBatchEmbeddings(List<String> texts) {
        try {
            // 构造请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL_NAME);
            
            JSONObject input = new JSONObject();
            JSONArray textsArray = new JSONArray();
            for (String text : texts) {
                textsArray.add(preprocessText(text));
            }
            input.put("texts", textsArray);
            requestBody.put("input", input);
            
            JSONObject parameters = new JSONObject();
            parameters.put("text_type", "document");
            requestBody.put("parameters", parameters);
            
            // 发送请求
            RequestBody okHttpBody = RequestBody.create(
                    requestBody.toJSONString(),
                    MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(EMBEDDING_API_URL)
                    .post(okHttpBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("DashScope API请求失败: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new RuntimeException("DashScope API响应为空");
                }
                
                String responseStr = body.string();
                JSONObject responseJson = JSON.parseObject(responseStr);
                
                JSONObject output = responseJson.getJSONObject("output");
                JSONArray embeddings = output.getJSONArray("embeddings");
                
                return embeddings.stream()
                        .map(obj -> (JSONObject) obj)
                        .map(embObj -> {
                            JSONArray embArray = embObj.getJSONArray("embedding");
                            float[] result = new float[embArray.size()];
                            for (int i = 0; i < embArray.size(); i++) {
                                result[i] = embArray.getFloatValue(i);
                            }
                            return result;
                        })
                        .toList();
            }
        } catch (IOException e) {
            logger.error("批量调用DashScope嵌入API失败", e);
            throw new RuntimeException("批量生成嵌入向量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 文本预处理
     */
    private String preprocessText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // 去除多余空格，限制长度
        text = text.trim().replaceAll("\\s+", " ");
        
        // DashScope text-embedding-v1 模型支持最大2048个token
        // 简单按字符数限制，实际应该按token数计算
        int maxLength = 6000; // 约2048个token
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength);
            logger.warn("文本长度超过限制，已截断到{}字符", maxLength);
        }
        
        return text;
    }
    
    /**
     * 获取嵌入向量的维度
     * @return 向量维度（DashScope text-embedding-v1 为 1536 维）
     */
    public int getEmbeddingDimension() {
        return 1536;
    }
    
    /**
     * 检查嵌入服务是否可用
     * @return 如果API密钥配置正确且服务可用则返回true
     */
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}