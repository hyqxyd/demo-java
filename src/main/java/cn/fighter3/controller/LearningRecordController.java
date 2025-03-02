package cn.fighter3.controller;

import cn.fighter3.entity.LearningRecord;
import cn.fighter3.entity.LearningRecordUpdate;
import cn.fighter3.result.Result;
import cn.fighter3.service.LearningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LearningRecordController {
    @Autowired
    private LearningRecordService learningRecordService;
    @PostMapping("/learning-records/save")
    public Result saveLearningRecords(@RequestBody LearningRecord learningRecord) {



        return new Result(200,"",learningRecordService.save(learningRecord));
    }

    @PostMapping("/learning-records/update/{id}")
    public Result updateLearningRecords(@PathVariable Integer id, @RequestBody LearningRecordUpdate learningRecordUpdate) {

        learningRecordService.update(learningRecordUpdate,id);

        return new Result(200,"","成功更新");
    }


    @GetMapping("/learning-records")
    public Result getLearningRecordsByProblemId(@RequestParam Integer problemId) {
       return new Result(200,"",learningRecordService.getLearningRecordsByProblemId(problemId));   }

    @GetMapping("/learning-records/student")
    public Result getLearningRecordsByStudentAndProblem(
            @RequestParam Integer studentId,
            @RequestParam Integer problemId
    ) {
        return new Result(200,"",learningRecordService.getLearningRecordsByUserId(studentId, problemId));
    }
    // LearningRecordController.java
    @GetMapping("/learning-records/frequency")
    public Result getFrequency(
            @RequestParam Integer studentId,
            @RequestParam Integer problemId
    ) {
        return new Result(200, "", learningRecordService.getDailyFrequency(studentId, problemId));
    }

    @GetMapping("/learning-records/model-usage")
    public Result getModelUsage(
            @RequestParam Integer studentId,
            @RequestParam Integer problemId
    ) {
        return new Result(200, "", learningRecordService.getModelUsageCount(studentId, problemId));
    }

    @GetMapping("/learning-records/duration")
    public Result getDuration(
            @RequestParam Integer studentId,
            @RequestParam Integer problemId
    ) {
        return new Result(200, "", learningRecordService.getDailyDuration(studentId, problemId));
    }

    // LearningRecordController.java
    @GetMapping("/learning-records/model-duration")
    public Result getModelDuration(
            @RequestParam Integer studentId,
            @RequestParam Integer problemId
    ) {
        return new Result(200, "", learningRecordService.getModelDuration(studentId, problemId));
    }

    @GetMapping("/learning-records/keywords")
    public Result getKeywords(
            @RequestParam Integer studentId,
            @RequestParam Integer problemId
    ) {
        return new Result(200, "", learningRecordService.getKeywords(studentId, problemId));
    }




}