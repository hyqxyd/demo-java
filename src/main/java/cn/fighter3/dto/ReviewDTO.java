package cn.fighter3.dto;

public class ReviewDTO {
    private Integer studentAnswerId;
    private String teacherFeedback;
    private String action; // "pass" or "reject"

    public Integer getStudentAnswerId() { return studentAnswerId; }
    public void setStudentAnswerId(Integer studentAnswerId) { this.studentAnswerId = studentAnswerId; }

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
