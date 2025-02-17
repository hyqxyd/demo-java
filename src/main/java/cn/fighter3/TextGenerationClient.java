package cn.fighter3;

import okhttp3.*;
import org.aspectj.bridge.IMessage;

import java.io.IOException;

public class TextGenerationClient {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = "sk-6a0845249f1d49b5bd2420e78597a523"; // 替换为您的API密钥

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String Message="{\"role\":\"user\",\"content\":\"计算机是什么？\"},{\"role\":\"assistant\",\"content\":\"计算机是一种**用于高速计算的现代化智能电子设备，可以进行数值和逻辑计算，并具有存储记忆功能**。\n\n计算机由硬件系统和软件系统组成，硬件包括输入设备、输出设备、运算器、控制器及存储器等核心部件。其中，中央处理器（CPU）是计算机的核心，负责执行程序指令和处理数据。而软件则是指运行在硬件上的程序和操作系统，它们共同协作以实现各种功能。\n\n随着科技的发展，计算机已经从最初的大型机演变为今天的个人电脑、笔记本电脑以及各种移动设备。计算机的应用范围也从军事科研扩展到社会的各个领域，成为信息社会中不可或缺的工具。\"},{\"role\":\"user\",\"content\":\"简单总结一下上面回答？\"}";

        String json = "{"
                + "  \"model\": \"qwen-plus\","
                + "  \"input\": {"
                + "    \"messages\": ["
                + "      {"
                + "        \"role\": \"system\","
                + "        \"content\": \"\""
                + "      },"
                + "      {"
                + "        \"role\": \"user\","
                + "        \"content\": \"计算机是什么？\\n\""
                + "      },"
                + "      {"
                + "        \"role\": \"assistant\","
                + "        \"content\": \"计算机是一种电子设备，能够接收、处理和存储数据，并根据预设的指令（程序）输出结果。它通过执行一系列逻辑运算和数学运算来完成各种任务，从简单的计算到复杂的图像处理、数据分析和人工智能应用。\\n\\n计算机的核心组成部分包括：\\n\\n1. **中央处理器（CPU）**：负责执行指令和进行计算。\\n2. **内存（RAM）**：用于临时存储正在处理的数据和程序。\\n3. **存储设备**：如硬盘或固态硬盘（SSD），用于长期保存数据和程序。\\n4. **输入设备**：如键盘、鼠标等，用户通过这些设备与计算机交互。\\n5. **输出设备**：如显示器、打印机等，用于展示或输出处理结果。\\n6. **操作系统**：如Windows、macOS、Linux等，管理硬件资源并提供用户界面。\\n\\n计算机的工作原理基于“输入-处理-输出”的模式。用户或外部设备提供输入，计算机通过内部硬件和软件进行处理，最终产生输出结果。现代计算机不仅可以执行数值计算，还可以处理文本、图像、音频、视频等多种类型的数据。\\n\\n随着技术的发展，计算机已经从早期的大型机演变为个人电脑、笔记本电脑、平板电脑、智能手机等各种形式，并广泛应用于各个领域，成为现代社会不可或缺的工具。\""
                + "      },"
                + "      {"
                + "        \"role\": \"user\","
                + "        \"content\": \"简单说说\""
                + "      },"
                + "      {"
                + "        \"role\": \"assistant\","
                + "        \"content\": \"计算机是一种电子设备，用来处理和存储数据。它通过执行程序来完成各种任务，比如计算、文字处理、上网等。主要组成部分包括：\\n\\n1. **中央处理器（CPU）**：执行指令和运算。\\n2. **内存（RAM）**：临时存储数据。\\n3. **存储设备**：如硬盘或SSD，保存文件和程序。\\n4. **输入设备**：如键盘和鼠标，用于输入信息。\\n5. **输出设备**：如显示器和打印机，展示结果。\\n\\n计算机的工作流程是接收输入、进行处理，然后输出结果。现代计算机种类繁多，包括台式机、笔记本、平板和手机等，广泛应用于日常生活和工作中。\""
                + "      },"
                + "      {"
                + "        \"role\": \"user\","
                + "        \"content\": \"谈谈上面的cpu\""
                + "      }"
                + "    ]"
                + "  },"
                + "  \"parameters\": {"
                + "    \"result_format\": \"message\","
                + "    \"top_p\": 0.8,"
                + "    \"temperature\": 0.7,"
                + "    \"enable_search\": false"
                + "  }"
                + "}";

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-DashScope-SSE", "enable")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("请求失败: " + response.code() + " " + response.message());
                return;
            }
            String responseBody = response.body().string();
            System.out.println("响应内容: " + responseBody);
        } catch (IOException e) {
            System.err.println("请求过程中发生错误: " + e.getMessage());
        }
    }
}