package cn.fighter3;

import cn.fighter3.entity.Session;
import com.alibaba.dashscope.exception.ApiException;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONObject;

import java.io.*;



public class File_analysis {


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

    public static String Flie_upload(String filename,String conversationId) throws IOException {
        MediaType mediaType = MediaType.parse("multipart/form-data");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("app_id","dc6d8359-b428-4afa-9387-cd0cdea8c9bf")
                .addFormDataPart("file",filename,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("src\\main\\resources\\upload\\wenxin\\"+filename)))
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

    public static void FileAnalysis(String filename,String conversationId) throws IOException{

        String jsonString  =Flie_upload(filename,conversationId);
        // 解析JSON字符串为JSONObject对象
        JSONObject obj = new JSONObject(jsonString);

        System.out.println(jsonString);
        // 提取id和conversation_id
        String id = obj.getString("id");




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
                    JSONObject jsonObject = new JSONObject("{" + line + "}");
                    String c = jsonObject.getJSONObject("data").toString();
                    jsonObject = new JSONObject(c);
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


            } catch (ApiException | IOException e) {
            // 使用日志框架记录异常信息
            System.err.println("调用生成服务时发生错误: " + e.getMessage());
        }



    }

    public static void main(String[] args) throws Exception {

        String jsonString = GetConversationId();
        System.out.println(jsonString);
        JSONObject obj = new JSONObject(jsonString);

        String conversationId = obj.getString("conversation_id");

        System.out.println(conversationId);



        FileAnalysis("1733814130026_Domenic Cotroneo.pdf",conversationId);
    }





}
