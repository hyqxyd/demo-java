package cn.fighter3.controller;

import cn.fighter3.dto.StudentWithLearnStatusDTO;
import cn.fighter3.entity.Problem;
import cn.fighter3.result.Result;
import cn.fighter3.service.ProblemService;
import cn.fighter3.service.ProblemStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProblemController {
    @Autowired
    private ProblemService problemService;
    @Autowired
    private ProblemStudentService problemStudentService;
    @GetMapping("/problems")
    public Result getProblemsByTopicId(@RequestParam("topicId") Integer topicId) {
        System.out.println("主题："+topicId);
        System.out.println("获取问题:"+problemService.getProblemsByTopicId(topicId));
        return new Result(200,"获取问题成功",problemService.getProblemsByTopicId(topicId));
    }

    @GetMapping("/problems/students")
    public Result getStudentsByProblemId(
            @RequestParam("problemId") Integer problemId) {
        List<StudentWithLearnStatusDTO> students = problemStudentService.getStudentsByProblemId(problemId);
        System.out.println("获取学生:"+students);
        return new Result(200,"获取学生成功", students);
    }



}
