package cn.fighter3.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
@Service
public class TongyiQianwenService {
    @Autowired
    private QuestionService questionService ;
    @Autowired
    private AnswerService answerService;

    private int modeId = 2;
    @Autowired
    private OkHttpClient okHttpClient;

    public  String callWithMessage(String prompt ) throws ApiException, NoApiKeyException, InputRequiredException {
        JSONObject jsonObj = new JSONObject(prompt);
        String content = jsonObj.getString("content");
        int user_id = jsonObj.getInt("id");
        //插入+保存问题
        int q_id= questionService.saveQuestion(user_id, 1,content );
        //获取插入问题的id
        System.out.println("问题插入成功");
        System.out.println(content);

        prompt = content;
        if (prompt.endsWith("\n")) {
            prompt = prompt.substring(0, prompt.length() - 1);
        }
        OkHttpClient client = new OkHttpClient();
        // 替换为你的API URL
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        // 替换为你的API密钥
        String dashscopeApiKey = "sk-6a0845249f1d49b5bd2420e78597a523";

        // 设置请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String json = "{"
                + "\"model\": \"qwen-plus\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"system\","
                + "        \"content\": \"You are a helpful assistant.\""
                + "    },"
                + "    {"
                + "        \"role\": \"user\", "
                + "        \"content\": \""+prompt+"\""
                + "    }"
                + "],"
                + "\"stream\": true"
                + "}";
        RequestBody body = RequestBody.create(json, JSON);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + dashscopeApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println(request.toString());

        String data = null;
        try(Response response=client.newCall(request).execute()){
            if (!response.isSuccessful()) {
                System.err.println("请求失败: " + response.code());
                throw new RuntimeException("请求失败: " + response);
            }
            data = "";
            // 处理流式响应
            ResponseBody responseBody = response.body();

            if (responseBody != null) {
                BufferedSource source = responseBody.source();

                while (!source.exhausted()) {
                    String line = source.readUtf8LineStrict();
                    System.out.println(line);
                    if (line.trim().isEmpty() || line.contains("[DONE]")) {
                        continue; // 跳过空行
                    }
                    // 解析JSON格式的行
                    JSONObject jsonObject = new JSONObject("{" + line + "}");
                    String c = jsonObject.getJSONObject("data").toString();
                    jsonObject = new JSONObject(c);
                    System.out.println(jsonObject.toString());
                    JSONObject choicesObject = jsonObject.getJSONArray("choices").getJSONObject(0);
                    System.out.println(choicesObject.toString());
                    JSONObject deltaObject = choicesObject.getJSONObject("delta");
                    System.out.println(deltaObject.toString());
                    c = deltaObject.getString("content");
                    System.out.println("Content: " + c);
                    data += c;
                }
                System.out.println(data);
                answerService.saveAnswer(q_id, data,modeId);
                System.out.println("答案保存成功！");
            }


        } catch (ApiException | IOException e) {
            // 使用日志框架记录异常信息
            System.err.println("调用生成服务时发生错误: " + e.getMessage());
        }


        return data;


    }


}
