package cn.fighter3;


import okhttp3.*;
import org.json.JSONObject;

import java.io.*;


/**
 * 需要添加依赖
 * <!-- https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp -->
 * <dependency>
 *     <groupId>com.squareup.okhttp3</groupId>
 *     <artifactId>okhttp</artifactId>
 *     <version>4.12.0</version>
 * </dependency>
 */

class Sample_1 {

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String []args) throws IOException{
        MediaType mediaType = MediaType.parse("multipart/form-data");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("app_id","dc6d8359-b428-4afa-9387-cd0cdea8c9bf")
                .addFormDataPart("response_mode","streaming")
                .addFormDataPart("query","总结一下")
                .addFormDataPart("conversation_id","305c46b6-3ba7-423b-b804-aa34f5f8a96b")

                .addFormDataPart("file","20 洪毅 文献阅读作业.docx",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("C:\\Users\\14820\\Desktop\\20 洪毅 文献阅读作业.docx")))
                .build();
        Request request = new Request.Builder()
                .url("https://appbuilder.bce.baidu.com/api/ai_apaas/console/share/instance/integrated")
                .method("POST", body)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("X-Appbuilder-Authorization", "Bearer bce-v3/ALTAK-lhfUrRTbPyKN9CUaJbtYp/82472b0c63a3d8ac3f18494cfa27853c3f04d0e5")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }


}