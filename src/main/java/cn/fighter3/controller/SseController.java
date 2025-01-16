package cn.fighter3.controller;

import cn.fighter3.service.KedaXunfeiService;
import cn.fighter3.service.TongyiQianwenService;
import cn.fighter3.service.WenxinYiyanService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
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

    @GetMapping("/api/sse")
    public SseEmitter getChatStream(@RequestParam String prompt)  {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        System.out.println(prompt);
        JSONObject jsonObject=new JSONObject(prompt);
        String service=jsonObject.getString("service");
        System.out.println(service);

        if("wenxin".equals(service)){


        try {
            wenxinYiyanService.sendStreamRequest(prompt, emitter);
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

        }


return emitter;

    }
}
