package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import java.util.Date;


@TableName("questions")
public class Question {

    @TableId(type = IdType.AUTO)
    private int questionId;
    private int userId;
    private int courseId;
    private String questionText;
    private Date questionTime;




    // Getters and Setters
    public int getQuestionId() {
        return this.questionId;
    }
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    public int getUserId() {
        return this.userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getCourseId() {
        return this.courseId;
    }
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    public String getQuestionText() {
        return this.questionText;
    }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    public Date getQuestionTime() {
        return this.questionTime;
    }
    public void setQuestionTime(Date questionTime) {
        this.questionTime = questionTime;
}
}