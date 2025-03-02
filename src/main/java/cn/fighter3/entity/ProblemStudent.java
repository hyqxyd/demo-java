package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("problem_student")
public class ProblemStudent {
    private Integer problemId;
    private Integer studentId;
    private Boolean learned;

    public ProblemStudent(Integer problemId, Integer studentId, Boolean learned) {
        this.problemId = problemId;
            this.studentId = studentId;
            this.learned = learned;

    }

    public Integer getProblemId() {
        return problemId;
    }
    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }
    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public Boolean getLearned() {
        return learned;
    }
}