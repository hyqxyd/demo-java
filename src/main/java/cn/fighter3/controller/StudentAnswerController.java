package cn.fighter3.controller;

import cn.fighter3.dto.AnswerRequest;
import cn.fighter3.dto.StudentAnswerDetailDTO;
import cn.fighter3.dto.StudentAnswerQueryDTO;
import cn.fighter3.result.Result;
import cn.fighter3.service.StudentAnswerService;
import cn.fighter3.vo.StudentAnswerVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student-answer")
public class StudentAnswerController {

    @Autowired
    private StudentAnswerService studentAnswerService;

    @PostMapping("/list")
    public Result getAnswers(@RequestBody StudentAnswerQueryDTO dto) {
        IPage<StudentAnswerVO> page = studentAnswerService.queryAnswerWithDetails(dto);
        return Result.success(page);
    }
    @GetMapping("/detail/{id}")
    public Result getDetail(@PathVariable Integer id) {
        StudentAnswerDetailDTO detail = studentAnswerService.getAnswerDetail(id);
        return Result.success(detail);
    }

    @PostMapping("/submit")
    public Result submitAnswer(@RequestBody AnswerRequest request) {
        boolean success = studentAnswerService.submitAnswer(request.getId(), request.getContent());
        return success ? Result.success("提交成功") : Result.error(500, "提交失败");
    }
    @GetMapping("/unfinished-count")
    public Result getUnfinishedCount(@RequestParam Integer studentId) {
        int count = studentAnswerService.countUnfinishedProblems(studentId);
        return Result.success(count);
    }

}
