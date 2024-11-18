package cn.fighter3.service;

import cn.fighter3.config.AppConfig;


import cn.fighter3.entity.Question;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.QuestionMapper;
import cn.fighter3.mapper.SessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import okhttp3.*;

import okio.BufferedSource;
import org.hibernate.sql.Update;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;



@Service
public class WenxinYiyanService {

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String EB_STREAM_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k";

    @Autowired
    private AppConfig config;
    @Autowired
    private QuestionService questionService ;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionMapper sessionMapper;

    private int modeId = 1;
    private  int flag=0;

    private OkHttpClient client = new OkHttpClient();

//    public String getAccessToken() throws Exception {
//        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
//        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + config.getApiKey() + "&client_secret=" + config.getSecretKey());
//        Request request = new Request.Builder()
//                .url(TOKEN_URL)
//                .post(body)
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            return new JSONObject(response.body().string()).getString("access_token");
//        }
//    }
public String getAccessToken() throws IOException {
    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
    RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + config.getApiKey()
            + "&client_secret=" + config.getSecretKey());
    Request request = new Request.Builder()
            .url("https://aip.baidubce.com/oauth/2.0/token")
            .method("POST", body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build();
    Response response = client.newCall(request).execute();
    return new JSONObject(response.body().string()).getString("access_token");
}

    public String sendStreamRequest(String prompt) throws Exception {
        String accessToken = getAccessToken();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        //查询历史记录


        JSONObject jsonObj = new JSONObject(prompt);
        String content = jsonObj.getString("content");
        int user_id=jsonObj.getInt("id");
        String s_id=jsonObj.getString("sessionId");


       //插入+保存问题
        int q_id= questionService.saveQuestion(user_id, 1,content );
        //获取插入问题的id
        System.out.println("问题插入成功");

        System.out.println(content);

        prompt=content;
        if (prompt.endsWith("\n")) {
            prompt = prompt.substring(0, prompt.length() - 1);
        }
       // RequestBody ebRequestBody = RequestBody.create(mediaType,jsonObject.toString());
        String message="{\"role\":\"user\",\"content\":\"" + prompt +"\"}";


        Session session=sessionMapper.selectOne(new QueryWrapper<Session>().eq("id", s_id).eq("user_id", user_id));
        String messages="";
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



        String json= "{\"messages\":["+messages+"],\"stream\":true}";
        System.out.println(json);
        RequestBody ebRequestBody = RequestBody.create(mediaType, json);
        System.out.println(ebRequestBody);
        Request request = new Request.Builder()
                .url(EB_STREAM_URL + "?access_token=" + accessToken)
                .method("POST",ebRequestBody)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();
        System.out.println(request.toString());
        //处理流式响应
        String answer="";

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    BufferedSource source= responseBody.source();
                    while (!source.exhausted()) {
                          String line = source.readUtf8Line();
                          System.out.println(line);
                        if (line.trim().isEmpty()||line.contains("[DONE]")) {
                            continue; // 跳过空行
                        }
                        // 解析JSON格式的行
                        JSONObject jsonLine = new JSONObject("{"+line+"}");
                        String data=jsonLine.getJSONObject("data").getString("result");
                        System.out.println(data);
                        answer+=data;
                    }
                }

                System.out.println(answer);
                //JSONObject r = new JSONObject(response.body().string());//获取回答
                //String answer = r.getString("result");
                int a_id=answerService.saveAnswer(q_id, answer,modeId);
                messages+=","+"{\"role\":\"assistant\",\"content\":\"" + answer +"\"}";
                if(flag==1) {
                    sessionService.saveSession(s_id, q_id, a_id, modeId, 1, user_id,messages);
                }else {
                    UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", s_id).eq("user_id", user_id);

                    Session newsession = session;
                    newsession.setContent(messages);
                    newsession.setSessionTime();

                    sessionMapper.update(newsession, updateWrapper);

                }
                //System.out.println(response.body().string().toString());
                return answer;
            } else {
                System.out.println("2");

                throw new RuntimeException("请求失败: " + response);
            }
        }
    }
}