package cn.fighter3.dto;

import lombok.Data;

@Data
public class HistoryDialogueRequest {
    private int studentId;
    private int courseId;
    private int topicId;
    private int problemId;


    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getCourseId() {
        return courseId;
    }
    

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    public int getStudentId() {
        return studentId;
    }
    
    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }
    public int getTopicId() {
        return topicId;
    }
    public void setProblemId(int problemId) {  
        this.problemId = problemId;
    }
    public int getProblemId() {
        return problemId;
    }
}