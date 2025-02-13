package cn.fighter3.service;

import cn.fighter3.entity.Message;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 替换为你的API密钥
        String dashscopeApiKey = "sk-6a0845249f1d49b5bd2420e78597a523";

       String messageArray = "[\n" +
                messages+"\n" +
//                "    {\n" +
//                "        \"role\": \"user\",\n" +
//                "        \"content\": \"计算机是什么？\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"role\": \"assistant\",\n" +
//                "        \"content\": \"计算机是一种电子设备，能够接收、处理和存储数据，并根据指令输出结果。它通过执行预定义的程序或算法来完成各种任务，从简单的计算到复杂的图像处理、数据分析等。\\n\\n计算机的基本组成部分包括：\\n\\n1. **硬件（Hardware）**：这是计算机的物理部分，包括中央处理器（CPU）、内存（RAM）、硬盘、输入设备（如键盘、鼠标）和输出设备（如显示器、打印机）等。\\n   \\n2. **软件（Software）**：这是计算机运行所需的程序和指令集。软件可以分为系统软件（如操作系统）和应用软件（如文字处理软件、浏览器等）。\\n\\n3. **数据（Data）**：计算机处理的信息，可以是数字、文本、图像、音频等各种形式。\\n\\n4. **网络（Network）**：计算机可以通过网络与其他设备连接，进行数据传输和资源共享。\\n\\n计算机的工作原理基于冯·诺依曼体系结构，即通过存储程序控制，计算机可以从内存中读取指令并执行相应的操作。现代计算机已经广泛应用于各个领域，如科学计算、工程设计、商业管理、娱乐和通信等。\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"role\": \"user\",\n" +
//                "        \"content\": \"简单说说\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"role\": \"assistant\",\n" +
//                "        \"content\": \"计算机是一种电子设备，能够按照指令处理数据。它主要包括："+
//                "\\n\\n1. **硬件**：物理部件，如CPU、内存、硬盘、键盘和显示器。\\n2. **软件**：运行的程序和系统，如操作系统和应用软件。\\n3. **数据**：处理的信息，如文字、图片和视频。\\n\\n计算机通过执行程序来完成各种任务，从简单的计算到复杂的图像处理和数据分析。现代计算机还可以通过网络与其他设备连接，进行数据传输和共享。\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"role\": \"user\",\n" +
//                "        \"content\": \"简单说说\"\n" +
//                "    }\n" +
                "]";
        // 设置请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String json = "{\n" +
                "    \"model\": \"qwen-plus\",\n" +
                "    \"input\": {\n" +
                "        \"messages\": " + messageArray+ "\n" +
                "    },\n" +
                "    \"parameters\": {\n" +
                "        \"result_format\": \"message\",\n" +
                "        \"incremental_output\": true\n" +
                "    },\n" +
                "    \"stream\": true\n" +
                "}";

        System.out.println(json);
        RequestBody body = RequestBody.create(json, JSON);
        System.out.println(body.toString());
        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + dashscopeApiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-DashScope-SSE","enable")
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
                                System.out.println(jsonObject.toString());
                                String output=jsonObject.getJSONObject("output").toString();
                                System.out.println(output);
                                jsonObject = new JSONObject(output);
                                JSONObject choicesObject = jsonObject.getJSONArray("choices").getJSONObject(0);
                                System.out.println(choicesObject.toString());

                                String finishreason = choicesObject.getString("finish_reason");

                                System.out.println(finishreason);
                                JSONObject deltaObject = choicesObject.getJSONObject("message");
                                if (finishreason.equals("stop")) {
                                    System.out.println("结束！");
                                    isend = true;
                                }
                                System.out.println(isend);
                                System.out.println(deltaObject.toString());

                                c = deltaObject.getString("content");
                                c=c.replace("\n", "\\n");
                                System.out.println("Content: " + c);

//
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
