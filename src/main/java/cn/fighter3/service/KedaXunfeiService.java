package cn.fighter3.service;

import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KedaXunfeiService {
    @Autowired
    private QuestionService questionService ;
    @Autowired
    private AnswerService answerService;
    private int modeId = 3;

    public String sendStreamRequest(String prompt) throws Exception {
        JSONObject jsonObj = new JSONObject(prompt);
         prompt = jsonObj.getString("content");
        int user_id=jsonObj.getInt("id");
        //插入+保存问题
        int q_id= questionService.saveQuestion(user_id, 1,prompt );
        //获取插入问题的id
        System.out.println("问题插入成功");

        System.out.println(prompt);


        if (prompt.endsWith("\n")) {
            prompt = prompt.substring(0, prompt.length() - 1);
        }


        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String jsonBody = "{\"model\": \"generalv3.5\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}], \"stream\": true}";
        RequestBody body = RequestBody.create(JSON, jsonBody);

        Request request = new Request.Builder()
                .url("https://spark-api-open.xf-yun.com/v1/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer wpuUVYmYaGKOhVVpqSFn:fQutxWlZOvFZBVosYOht") // 注意此处替换自己的API密钥
                .build();

        String data = null;
        try (Response response = client.newCall(request).execute()) {
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
                    String content = jsonObject.getJSONObject("data").toString();
                    jsonObject = new JSONObject(content);
                    JSONObject choicesObject = jsonObject.getJSONArray("choices").getJSONObject(0);
                    JSONObject deltaObject = choicesObject.getJSONObject("delta");

                    content = deltaObject.getString("content");
                    System.out.println("Content: " + content);
                    data += content;
                }
                System.out.println(data);
                answerService.saveAnswer(q_id, data,modeId);
                System.out.println("答案保存成功！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return data;
    }


}
