package cn.fighter3.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentAnswerVO {
    private Integer id;
    private Integer studentId;
    private Integer problemId;
    private String answerContent;
    private String status;
    private String teacherFeedback;
    private BigDecimal score;
    private LocalDateTime updatedTime;

    // 联表字段
    private String problemContent;
    private String topicName;
    private String courseName;
}
