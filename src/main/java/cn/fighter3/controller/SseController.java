package cn.fighter3.controller;

import cn.fighter3.service.KedaXunfeiService;
import cn.fighter3.service.KimiService;
import cn.fighter3.service.TongyiQianwenService;
import cn.fighter3.service.WenxinYiyanService;
import org.apache.ibatis.jdbc.Null;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
public class SseController {

    @Autowired
    private WenxinYiyanService wenxinYiyanService;
    @Autowired
    private TongyiQianwenService tongyiQianwenService;
    @Autowired
    private KedaXunfeiService kedaXunfeiService;
    @Autowired
    private KimiService kimiService;
    @GetMapping("/api/sse")
    public SseEmitter getChatStream(@RequestParam String prompt)  {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        System.out.println(prompt);
        JSONObject jsonObject=new JSONObject(prompt);
        String service=jsonObject.getString("service");
        System.out.println(service);
        if (jsonObject.get("courseId")==""){
            Exception exception = new Exception("请选择课程");
            emitter.completeWithError(exception);
        }else if (jsonObject.getInt("topicId")==0){
            Exception exception = new Exception("请选择主题");
            emitter.completeWithError(exception);
        }else if (jsonObject.getInt("problemId")==0){
            Exception exception = new Exception("请选择问题");
            emitter.completeWithError(exception);

    }







        if("wenxin".equals(service)){


        try {
            wenxinYiyanService.callWithMessage(prompt, emitter);
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

    } else if("tongyi".equals(service)){


            try {
                tongyiQianwenService.callWithMessage(prompt, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }

        }
        else if("xunfei".equals(service)){

            try {
                kedaXunfeiService.sendStreamRequest(prompt,emitter);
            }catch (Exception e){
                emitter.completeWithError(e);
            }

        }else if ("kimi".equals(service)){
            try {
                kimiService.callWithMessage(prompt,emitter);

            }catch (Exception e){
                emitter.completeWithError(e);
            }


        }


return emitter;

    }
}
