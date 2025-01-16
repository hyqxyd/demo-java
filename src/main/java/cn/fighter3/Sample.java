package cn.fighter3;

import okhttp3.*;
import org.json.JSONObject;

import java.io.*;
import java.util.Date;


/**
 * 需要添加依赖
 * <!-- https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp -->
 * <dependency>
 *     <groupId>com.squareup.okhttp3</groupId>
 *     <artifactId>okhttp</artifactId>
 *     <version>4.12.0</version>
 * </dependency>
 */

class Sample {
    public static final String API_KEY = "HQ3DFJEubFRenfczMOVN5dt6";
    public static final String SECRET_KEY = "ujmvju3BTfD4TjhTbvIGr6fe8V4UGr7C";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String []args) throws IOException{
        MediaType mediaType = MediaType.parse("application/json");
        Date newDate = new Date();
        System.out.println(newDate.toString());
        String prompt = "{\"role\":\"user\",\"content\":\"再说简单点？\"}";
        //String message = "{\"messages\":[{\"role\":\"user\",\"content\":\"计算机是什么？\"},{\"role\":\"assistant\",\"content\":\"计算机是一种用于处理数据、执行各种软件应用程序、模拟真实世界过程以及执行一系列算术和逻辑操作的电子机器。它通常由中央处理器（CPU）、内存、存储设备、输入设备和输出设备等基本组成部分组成。计算机可以进行诸如文字处理、数据分析、视频编辑和机器学习等多种任务，是一种通用的智能工具。它们已广泛应用于各行各业以及人们的日常生活中，包括但不限于办公室、家庭、学校、医院等场合。随着科技的发展，计算机的功能和应用范围不断扩大，它们在人们的日常生活和工作中的重要性也越来越高。\"},{\"role\":\"user\",\"content\":\"计算机的组成\"},{\"role\":\"assistant\",\"content\":\"计算机的组成可以分为硬件和软件两大部分。\\n\\n硬件是计算机的实体，是计算机系统的物理结构，主要包括中央处理器（CPU）、内存、存储设备、输入设备和输出设备等。其中，CPU是计算机的核心部件，负责执行计算机程序中的指令；内存则是用于存储和读取数据；存储设备如硬盘、固态硬盘等用于长期存储数据和程序；输入设备如键盘、鼠标等用于输入信息和操作指令；输出设备如显示器、打印机等用于展示和输出信息。\\n\\n软件则是计算机程序和数据结构的集合，包括操作系统、应用软件和各种文档等。操作系统是计算机上的一个基本软件，负责管理计算机的硬件资源，并为用户提供操作界面；应用软件则是为了满足用户特定的需求而开发的软件，如办公软件、图像处理软件等；文档则是计算机中存储和管理的各种文件和数据。\\n\\n除此之外，计算机还包括许多其他的部件和组件，如主板、显卡、声卡、电源等。这些部件和组件协同工作，共同构成了一个完整的计算机系统。总的来说，计算机的组成涉及到硬件、软件以及各个部件和组件的设计和配置。\"},{\"role\":\"user\",\"content\":\"请根据上面的对话，总结一下\"},{\"role\":\"assistant\",\"content\":\"计算机是一种电子机器，用于处理数据、执行软件应用程序、模拟真实世界过程以及执行一系列算术和逻辑操作。计算机的组成包括硬件和软件两大部分。硬件包括中央处理器、内存、存储设备、输入设备和输出设备等基本组成部分，而软件则包括操作系统、应用软件和各种文档等。计算机各个部件和组件协同工作，共同构成了一个完整的计算机系统。随着科技的发展，计算机的功能和应用范围不断扩大，它们在人们的日常生活和工作中的重要性也越来越高。\"},"+ prompt + "]}";
        String message="{\"messages\":[{\"role\":\"user\",\"content\":\"计算机是什么？\"},{\"role\":\"user\",\"content\":\"计算机是一种用于存储、处理和传输数据的电子机器。它由硬件和软件组成，能够进行各种运算、处理数据、生成结果以及控制输入和输出。计算机的核心部件包括中央处理器（CPU）、内存、硬盘、显卡等，它们协同工作以完成各种任务。计算机可以执行各种应用程序，如文字处理、图形设计、视频播放、网络通信等，也可以用于科研、教育、娱乐等各个领域。现在，计算机已经深入到社会的各个方面，成为现代社会不可或缺的一部分。\"},\n" +
                "{\"role\":\"user\",\"content\":\"总结一下上面的对话\"},{\"role\":\"user\",\"content\":\"总结一下上面的对话\"}],\"stream\":true}";
        RequestBody body = RequestBody.create(mediaType,message);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }


    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }

}