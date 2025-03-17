package cn.fighter3.controller;

import cn.fighter3.result.Result;
import cn.fighter3.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CognitiveController {
    @Autowired
    DeepSeekService deepSeekService;
    @PostMapping("/api/cognitive")
    public Result cognitive(@RequestParam("userId")  Integer userId,@RequestParam("id") String id){
        return new Result(200,"认知判断结果",deepSeekService.processSession(userId,id));
    }









}
