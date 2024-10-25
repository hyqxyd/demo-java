package cn.fighter3.controller;

import cn.fighter3.dto.QuestionDetail;
import cn.fighter3.result.Result;
import cn.fighter3.service.InformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    @Autowired
    private InformationService informationService;

    @GetMapping("/user/{userId}")
    public Result getUserQuestionsAndAnswers(@PathVariable int userId) {
        System.out.println(userId);
        List<QuestionDetail> questionDetails = informationService.getUserQuestionsAndAnswers(userId);
        return new Result(200, "Success", questionDetails);
}
}