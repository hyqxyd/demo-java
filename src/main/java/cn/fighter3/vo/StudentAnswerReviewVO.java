package cn.fighter3.vo;

import java.util.Date;

public class StudentAnswerReviewVO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String answerText;
    private Date answerTime;
    private String teacherFeedback;
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Date getAnswerTime() { return answerTime; }
    public void setAnswerTime(Date answerTime) { this.answerTime = answerTime; }

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
