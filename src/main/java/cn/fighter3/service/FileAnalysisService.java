package cn.fighter3.service;

import cn.fighter3.config.AppConfig;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
public class FileAnalysisService {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private AppConfig config;

    private static final String UPLOAD_URL = "https://qianfan.baidubce.com/v2/app/conversation/file/upload";
    private static final String ANALYSIS_URL = "https://qianfan.baidubce.com/v2/app/conversation/runs";
    private int modeId;


    @Transactional
    public void analyzeAndSave(String service,String filename,int modeId, int userId,String sessionId,int courseId,int topicId,String query) throws IOException {
        this.modeId = modeId;
        // 保存问题记录
        int questionId = questionService.saveQuestion(userId, courseId, query);

        String jsonString = GetConversationId();
        System.out.println(jsonString);
        org.json.JSONObject obj = new org.json.JSONObject(jsonString);

        String conversationId = obj.getString("conversation_id");
        // 文件上传并获取元数据
         String analysisResult=FileAnalysis(filename,service,query,conversationId);



        // 保存答案记录
        int answerId = answerService.saveAnswer(questionId, analysisResult, modeId);

        // 更新会话记录
        updateSession(sessionId, userId, topicId, questionId, answerId, query, analysisResult);
    }

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static String GetConversationId ()throws IOException{
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"app_id\":\"dc6d8359-b428-4afa-9387-cd0cdea8c9bf\"}");
        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/app/conversation")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer bce-v3/ALTAK-lhfUrRTbPyKN9CUaJbtYp/82472b0c63a3d8ac3f18494cfa27853c3f04d0e5")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String responseString = response.body().string();
        return responseString;


    }

    static final OkHttpClient HTTP_CLIENT_1 = new OkHttpClient().newBuilder().build();
    public static String Flie_upload(String filename ,String service,String conversationId ) throws IOException {
        MediaType mediaType = MediaType.parse("multipart/form-data");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("app_id","dc6d8359-b428-4afa-9387-cd0cdea8c9bf")
                .addFormDataPart("file",filename,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("opt\\springboot\\app\\src\\main\\resources\\upload\\"+service+"\\"+filename)))
                .addFormDataPart("conversation_id",conversationId)
                .build();

        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/app/conversation/file/upload")
                .method("POST", body)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Authorization", "Bearer bce-v3/ALTAK-lhfUrRTbPyKN9CUaJbtYp/82472b0c63a3d8ac3f18494cfa27853c3f04d0e5")
                .build();
        Response response = HTTP_CLIENT_1.newCall(request).execute();
        String responseString = response.body().string();
        System.out.println(responseString);//获得一个上传的文件的对话id和上传文件id
        return responseString;
    }
    static final OkHttpClient HTTP_CLIENT_2 = new OkHttpClient().newBuilder().build();
    public  String FileAnalysis(String  filename,String service,String query,String conversationId) throws IOException{

        String jsonString  =Flie_upload(filename,service,conversationId);
        System.out.println(jsonString);
        // 解析JSON字符串为JSONObject对象
        org.json.JSONObject obj = new org.json.JSONObject(jsonString);

        // 提取id和conversation_id
        String id = obj.getString("id");


        System.out.println(conversationId);



        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"app_id\":\"dc6d8359-b428-4afa-9387-cd0cdea8c9bf\",\"query\":\"总结\",\"conversation_id\":\""+conversationId+"\",\"stream\":true,\"file_ids\":[\""+id+"\"]}");
        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/app/conversation/runs")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Appbuilder-Authorization", "Bearer bce-v3/ALTAK-lhfUrRTbPyKN9CUaJbtYp/82472b0c63a3d8ac3f18494cfa27853c3f04d0e5")
                .build();
        try(Response response=HTTP_CLIENT_2.newCall(request).execute()){
            if (!response.isSuccessful()) {
                System.err.println("请求失败: " + response.code());
                throw new RuntimeException("请求失败: " + response);
            }
            String data = "";
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
                    org.json.JSONObject jsonObject = new org.json.JSONObject("{" + line + "}");
                    String c = jsonObject.getJSONObject("data").toString();
                    jsonObject = new org.json.JSONObject(c);
                    System.out.println(jsonObject.toString());
                    boolean completionStatus = jsonObject.getBoolean("is_completion");
                    if (completionStatus) {
                        break;
                    }
                    c=jsonObject.getString("answer");
                    c=c.replace("\n","\\n");
                    data += c;
                }
                System.out.println(data);
            }
            return data;

        } catch (ApiException | IOException e) {
            // 使用日志框架记录异常信息

            System.err.println("调用生成服务时发生错误: " + e.getMessage());
            return "调用生成服务时发生错误: " + e.getMessage();
        }
    }



        private void updateSession(String sessionId, int userId, int topicId,
                               int questionId, int answerId, String query, String analysisResult) {
        Session session = sessionMapper.selectOne(
                new QueryWrapper<Session>()
                        .eq("id", sessionId)
                        .eq("user_id", userId));

        JSONArray messages = (session == null) ?
                new JSONArray() :
                JSON.parseArray(session.getContent());

        // 添加用户消息（包含文件查询）
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", escapeContent(query));
        messages.add(userMsg);

        // 添加助手消息（文件分析结果）
        JSONObject assistantMsg = new JSONObject();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", escapeContent(analysisResult));
        messages.add(assistantMsg);

        // 保存或更新会话
        if (session == null) {
            sessionService.saveSession(sessionId, questionId, answerId, modeId, topicId, userId, messages.toJSONString());
        } else {
            session.setContent(messages.toJSONString());
            sessionMapper.updateById(session);
        }
    }

    private String escapeContent(String content) {
        return content.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}