

// file: KimiService.java
package cn.fighter3.service;

import cn.fighter3.entity.Message;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.dashscope.exception.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class KimiService {
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private SessionService sessionService;

    private final String API_URL = " https://api.moonshot.cn/v1/chat/completions";
    private final String API_KEY = "sk-ZW3wWtSxB7Cmskq8zasCqk2qEx2Qi15FMSqelzh0bDvK2JBK"; // 替换实际API密钥
    private final String MODEL_NAME = "moonshot-v1-8k";
    private final int modeId = 4; // Kimi模式标识

    public void callKimiApi(String prompt, SseEmitter sseEmitter) {
        int flag=0;//是否是第一次提问
        JSONObject jsonObj = new JSONObject(prompt);
        String content = jsonObj.getString("content");
        int user_id = jsonObj.getInt("id");
        String s_id=jsonObj.getString("sessionId");
        int c_id=jsonObj.getInt("courseId");
        int t_id=jsonObj.getInt("topicId");

        //插入+保存问题
        int q_id= questionService.saveQuestion(user_id, c_id,content );
        //获取插入问题的id
        System.out.println("问题插入成功");
        System.out.println(content);



        // 调用API
        prompt = content;
        if (prompt.endsWith("\n")) {
            prompt = prompt.substring(0, prompt.length() - 1);
        }
        String message="{\"role\":\"user\",\"content\":\"" + prompt +"\"}";
        Session session=sessionMapper.selectOne(new QueryWrapper<Session>().eq("id", s_id).eq("user_id", user_id));
        String messages="";
        if(session==null){
            System.out.println("新对话");

            flag=1;//第一次提问
            System.out.println(flag);
            messages+=message;
        }else {
            System.out.println(flag);
            System.out.println("历史对话");
            messages=session.getContent();
            if(messages.endsWith("\n")){
                messages=messages.substring(0,messages.length()-1);
            }
            messages=messages+","+message;
        }

        System.out.println("上下文："+messages);



        OkHttpClient client = new OkHttpClient();
        // 替换为你的API URL
        String url = API_URL;


        // 设置请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        // 使用StringBuilder构建请求体（文献7）
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{")
                .append("\"model\": \"moonshot-v1-8k\",")
                .append("\"messages\": [")
                .append("{\"role\": \"system\", \"content\": \"你是课程答疑机器人\"},") // 系统提示词
                .append(messages) // 用户输入
                .append("],")
                .append("\"temperature\": 0.3,")
                .append("\"stream\": true")
                .append("}");

// 发送请求（文献8）
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);

        Request request = new Request.Builder()
                .url("https://api.moonshot.cn/v1/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();


        System.out.println(request.toString());
        final  String messages_c=messages;
        final int flag_c=flag;
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {


            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String data = "";
                    // 处理流式响应
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        BufferedSource source = responseBody.source();
                        boolean isend = false;
                        while (!source.exhausted()) {

                            String line = source.readUtf8LineStrict();
                            System.out.println("数据" + line);

                            if (line.startsWith("data:")) {
                                line = line.replace("\n", "\\n");
                                System.out.println(line);
                                if (line.trim().isEmpty() || line.contains("[DONE]")) {
                                    continue; // 跳过空行
                                }
                                // 解析JSON格式的行
                                JSONObject jsonObject = new JSONObject("{" + line + "}");
                                String c = jsonObject.getJSONObject("data").toString();
                                jsonObject = new JSONObject(c);
                                JSONObject choicesObject = jsonObject.getJSONArray("choices").getJSONObject(0);
                                System.out.println(choicesObject.toString());

                                Object  finishReasonObj  = choicesObject.opt("finish_reason");
                                String finishreason = (finishReasonObj  != null) ? finishReasonObj.toString() : "";

                                System.out.println(finishreason);
                                JSONObject deltaObject = choicesObject.getJSONObject("delta");

                                if (finishreason.equals("stop")) {
                                    System.out.println("结束！");
                                    isend = true;
                                }
                                System.out.println(isend);
                                System.out.println(deltaObject.toString());
                                 c="";
                                if(isend!=true){
                                    c = deltaObject.getString("content");
                                    c = c.replace("\n", "\\n");
                                    System.out.println("Content: " + c);
                                }
//
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                sseEmitter.send(SseEmitter.event().data("{\"content\":\"" + c + "\",\"is_end\":\"" + isend + "\"}"));

                                data += c;

                            }
                        }
                        sseEmitter.complete();

                    }


                    System.out.println(data);
                    int a_id = answerService.saveAnswer(q_id, data, modeId);
                    System.out.println("答案保存成功！");
                    String history = messages_c + "," + "{\"role\":\"system\",\"content\":\"" + data + "\"}";
                    System.out.println(flag_c);
                    if (flag_c == 1) {
                        sessionService.saveSession(s_id, q_id, a_id, modeId, t_id, user_id, history);
                    } else {
                        UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("id", s_id).eq("user_id", user_id);

                        Session newsession = session;
                        newsession.setContent(history);
                        newsession.setSessionTime();

                        sessionMapper.update(newsession, updateWrapper);

                    }

                }else {
                    System.out.println(response);
                    System.out.println("2");
                    sseEmitter.completeWithError(new RuntimeException("请求失败: " + response));
                    throw new RuntimeException("请求失败: " + response);
                }
            } catch (ApiException | IOException e) {
                // 使用日志框架记录异常信息
                sseEmitter.completeWithError(e);
                System.err.println("调用生成服务时发生错误: " + e.getMessage());
            }

        });

    }


}