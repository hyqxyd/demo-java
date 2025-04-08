package cn.fighter3.controller;

import cn.fighter3.result.Result;
import cn.fighter3.dto.TeacherAnswerQueryDTO;
import cn.fighter3.dto.ReviewDTO;
import cn.fighter3.service.TeacherAnswerService;
import cn.fighter3.vo.StudentAnswerReviewVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/answers")
public class TeacherAnswerController {

    @Autowired
    private TeacherAnswerService teacherAnswerService;

    @PostMapping("/list")
    public Result listStudentAnswers(@RequestBody TeacherAnswerQueryDTO queryDTO) {
        IPage<StudentAnswerReviewVO> page = teacherAnswerService.getStudentAnswers(queryDTO);
        return Result.success(page);
    }

    @PostMapping("/review")
    public Result reviewStudentAnswer(@RequestBody ReviewDTO reviewDTO) {
        boolean result = teacherAnswerService.reviewAnswer(reviewDTO);
        return result ? Result.success("操作成功") : Result.error(500, "操作失败");
    }
}
