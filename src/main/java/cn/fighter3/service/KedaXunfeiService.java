package cn.fighter3.service;

import cn.fighter3.service.BigModelRequest;

import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Service
public class KedaXunfeiService {
    @Autowired
    private QuestionService questionService ;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionMapper sessionMapper;
    private BigModelRequest bigModelRequest;
    private   String data = null;
    private String messages="";
    private  int flag=0;
    private int modeId = 3;

    public void sendStreamRequest(String prompt ,SseEmitter sseEmitter) throws Exception {
        JSONObject jsonObj = new JSONObject(prompt);
        prompt = jsonObj.getString("content");
        int user_id=jsonObj.getInt("id");
        String s_id=jsonObj.getString("sessionId");
        //插入+保存问题
        int q_id= questionService.saveQuestion(user_id, 1,prompt );
        //获取插入问题的id
        System.out.println("问题插入成功");

        System.out.println(prompt);


        if (prompt.endsWith("\n")) {
            prompt = prompt.substring(0, prompt.length() - 1);
        }
        String message="{\"role\":\"user\",\"content\":\"" + prompt +"\"}";
        Session session=sessionMapper.selectOne(new QueryWrapper<Session>().eq("id", s_id).eq("user_id", user_id));
        messages="";
        if(session==null){
            System.out.println("新对话");

            flag=1;//第一次提问
            messages+=message;
        }else {
            System.out.println("历史对话");
            messages=session.getContent();
            if(messages.endsWith("\n")){
                messages=messages.substring(0,messages.length()-1);
            }
            messages=messages+","+message;
        }
//        OkHttpClient client = new OkHttpClient();
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


//
//        String jsonBody = "{\"model\": \"generalv3.5\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}], \"stream\": true}";
//        RequestBody body = RequestBody.create(JSON, jsonBody);

        System.out.println(messages);

        JSONArray textArray = JSON.parseArray("["+messages+"]");


        bigModelRequest=new BigModelRequest(answerService,sessionService,sessionMapper,textArray,sseEmitter,s_id,modeId,user_id,q_id,messages,session,flag);

////        data=bigModelRequest.getData();
//        System.out.println("获得的回答："+data);
//        int a_id=answerService.saveAnswer(q_id, data,modeId);
//        System.out.println("答案保存成功！");
//        messages+=","+"{\"role\":\"assistant\",\"content\":\"" + data +"\"}";
//        System.out.println(flag);
//        if(flag==1) {
//            sessionService.saveSession(s_id, q_id, a_id, modeId, 1, user_id,messages);
//        }else {
//            UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.eq("id", s_id).eq("user_id", user_id);
//
//            Session newsession = session;
//            newsession.setContent(messages);
//            newsession.setSessionTime();
//
//            sessionMapper.update(newsession, updateWrapper);
//
//        }

//       while (true){
//            if(messageCallback.getMessage()=="回答结束"){
//                data = bigModelRequest.getData();
//                break;
//            }
//
//       }



//        Request request = new Request.Builder()
//                .url("https://spark-api-open.xf-yun.com/v1/chat/completions")
//                .post(body)
//                .addHeader("Authorization", "Bearer wpuUVYmYaGKOhVVpqSFn:fQutxWlZOvFZBVosYOht") // 注意此处替换自己的API密钥
//                .build();


//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                System.err.println("请求失败: " + response.code());
//                throw new RuntimeException("请求失败: " + response);
//            }
//            data = "";
//            // 处理流式响应
//            ResponseBody responseBody = response.body();
//
//            if (responseBody != null) {
//                BufferedSource source = responseBody.source();
//
//                while (!source.exhausted()) {
//                    String line = source.readUtf8LineStrict();
//                    System.out.println(line);
//                    if (line.trim().isEmpty() || line.contains("[DONE]")) {
//                        continue; // 跳过空行
//                    }
//                    // 解析JSON格式的行
//                    JSONObject jsonObject = new JSONObject("{" + line + "}");
//                    String content = jsonObject.getJSONObject("data").toString();
//                    jsonObject = new JSONObject(content);
//                    JSONObject choicesObject = jsonObject.getJSONArray("choices").getJSONObject(0);
//                    JSONObject deltaObject = choicesObject.getJSONObject("delta");
//
//                    content = deltaObject.getString("content");
//                    System.out.println("Content: " + content);
//                    data += content;
//                }
//                System.out.println(data);
//                int a_id=answerService.saveAnswer(q_id, data,modeId);
//                System.out.println("答案保存成功！");
//                messages+=","+"{\"role\":\"assistant\",\"content\":\"" + data +"\"}";
//                System.out.println(flag);
//                if(flag==1) {
//                    sessionService.saveSession(s_id, q_id, a_id, modeId, 1, user_id,messages);
//                }else {
//                    UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
//                    updateWrapper.eq("id", s_id).eq("user_id", user_id);
//
//                    Session newsession = session;
//                    newsession.setContent(messages);
//                    newsession.setSessionTime();
//
//                    sessionMapper.update(newsession, updateWrapper);
//
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


}
