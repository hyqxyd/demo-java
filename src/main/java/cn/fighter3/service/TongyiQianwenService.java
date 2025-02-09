package cn.fighter3.service;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TongyiQianwenService {
    @Autowired
    private QuestionService questionService ;
    @Autowired
    private AnswerService answerService;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionMapper sessionMapper;
    private int modeId = 2;
    private  int flag=0;
    @Autowired
    private OkHttpClient okHttpClient;

    public  void callWithMessage(String prompt , SseEmitter sseEmitter) throws ApiException, NoApiKeyException, InputRequiredException {
        JSONObject jsonObj = new JSONObject(prompt);
        String content = jsonObj.getString("content");
        int user_id = jsonObj.getInt("id");
        String s_id=jsonObj.getString("sessionId");
        //插入+保存问题
        int q_id= questionService.saveQuestion(user_id, 1,content );
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
               +messages
                + "],"
                + "\"stream\": true"
                + "}";
        RequestBody body = RequestBody.create(json, JSON);
        System.out.println(body.toString());
        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + dashscopeApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println(request.toString());
        final  String messages_c=messages;

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
                            String finishreason = choicesObject.optString("finish_reason", null);

                            System.out.println(finishreason);
                            JSONObject deltaObject = choicesObject.getJSONObject("delta");
                            if ( finishreason!= null) {
                                System.out.println("结束！");
                                isend = true;
                            }
                            System.out.println(isend);
                            System.out.println(deltaObject.toString());

                            c = deltaObject.getString("content");

                            System.out.println("Content: " + c);
//
                            sseEmitter.send(SseEmitter.event().data("{\"content\":\"" + c + "\",\"is_end\":\"" + isend + "\"}"));

                            data += c;

                        }
                        sseEmitter.complete();
                    }


                        System.out.println(data);
                        int a_id = answerService.saveAnswer(q_id, data, modeId);
                        System.out.println("答案保存成功！");
                        String history = messages_c + "," + "{\"role\":\"system\",\"content\":\"" + data + "\"}";
                        System.out.println(flag);
                        if (flag == 1) {
                            sessionService.saveSession(s_id, q_id, a_id, modeId, 1, user_id, history);
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
