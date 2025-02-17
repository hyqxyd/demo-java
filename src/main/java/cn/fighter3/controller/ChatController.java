package cn.fighter3.controller;

import cn.fighter3.entity.Question;
import cn.fighter3.result.Result;
import cn.fighter3.service.KedaXunfeiService;
import cn.fighter3.service.TongyiQianwenService;
import cn.fighter3.service.WenxinYiyanService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private WenxinYiyanService wenxinYiyanService;
    @Autowired
    private TongyiQianwenService tongyiQianwenService;
    @Autowired
    private KedaXunfeiService kedaXunfeiService;


    private Question question;
    @PostMapping("/wenxin")
    public Result wenxin(@RequestBody String prompt) {
        try {

            System.out.println(prompt);
//            String response = wenxinYiyanService.sendStreamRequest(prompt);
//            System.out.println(response);
            //return ResponseEntity.status(200).body(response);
            return new Result(200,"请求成功","response");
        } catch (Exception e) {
            e.printStackTrace();
            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
              return new Result(500,"","服务器错误");
        }
    }



    @PostMapping("/tongyi")
    public Result tongyi(@RequestBody String prompt) {
//        try {
//
//            System.out.println(prompt);
//            String response = tongyiQianwenService.callWithMessage(prompt);
//            System.out.println(response);
//            //return ResponseEntity.status(200).body(response);
//            return new Result(200,"请求成功",response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
//            return new Result(500,"","服务器错误");
//        }
        return new Result(200,"请求成功",null);
    }




    @PostMapping("/xunfei")
    public Result xunfei(@RequestBody String prompt) {
//        try {
//
//            System.out.println(prompt);
//            String response = kedaXunfeiService.sendStreamRequest(prompt);
//            System.out.println(response);
//            //return ResponseEntity.status(200).body(response);
//            return new Result(200,"请求成功",response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
            return new Result(500,"","服务器错误");
//        }
    }


}