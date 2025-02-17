package cn.fighter3;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class MainClass {
   public static void main(String[] args) throws Exception {
//         String answer="";
////       String text = "["+"{\"role\":\"user\",\"content\":\"计算机是什么？\"}"+"]";
////        JSONArray textArray = new JSONArray();
////        JSONObject text = new JSONObject();
////        text.put("role", "user");
////        text.put("content", "环境治理的重要性");
////        textArray.add(text);
        String[] messages = {
                "{\"role\":\"user\",\"content\":\"计算机是什么？\"}",
                "{\"role\":\"assistant\",\"content\":\"计算机是一种电子设备，能够接收、处理和存储数据以产生有用的信息。它由硬件和软件组成，硬件包括中央处理器（CPU）、内存、输入输出设备等，而软件则是指操作系统和应用程序等。计算机可以执行各种任务，例如文字处理、图像处理、数据分析、网络通信等。在现代社会中，计算机被广泛应用于各个领域，如科学、工程、商业、娱乐等。\"}",
                "{\"role\":\"user\",\"content\":\"总结一下上面的回答\"}",
                "{\"role\":\"assistant\",\"content\":\"计算机是能够接收、处理和存储数据以产生信息的电子设备，由硬件（如CPU、内存、输入输出设备）和软件（如操作系统、应用程序）组成，可执行文字处理、图像处理、数据分析、网络通信等多种任务，在科学、工程、商业、娱乐等领域广泛应用。\"}",
                "{\"role\":\"user\",\"content\":\"再简单一点\"}",

        };

        JSONArray textArray = new JSONArray();
        for (String message : messages) {
            textArray.add(JSONObject.parseObject(message));
        }
        System.out.println(textArray.toJSONString());


        BigModelRequest_1 request;

    {
        try {
            request = new BigModelRequest_1(textArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
}