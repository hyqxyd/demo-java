package cn.fighter3.dto;

import lombok.Data;

@Data
public class StudentAnswerDetailDTO {
    private Long id;
    private String courseName;
    private String topicName;
    private String problemContent;
    private String answerContent;
    private String teacherFeedback;
    private Integer score;
    private String status;
}
