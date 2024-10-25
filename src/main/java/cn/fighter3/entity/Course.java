package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("courses")
public class Course {
    @TableId(type = IdType.AUTO)
    private int courseId;
    private String courseName;
    private String courseDescription;
    private int teacherId;

    // Getters and Setters

    public int getCourseId() {
        return this.courseId ;
    }
    public void setCourseId(int courseId) {
        this.courseId= courseId;
    }
    public String getCourseName() {
        return this.courseName ;
    }
    public void setCourseName(String courseName) {
        this.courseName= courseName;
    }
    public String getCourseDescription() {
        return this.courseDescription ;
    }
    public void setCourseDescription(String courseDescription) {
        this.courseDescription= courseDescription;
    }
    public int getTeacherId() {
        return this.teacherId ;
    }
    public void setTeacherId(int teacherId) {
        this.teacherId= teacherId;
    }
    public Course() {}
    public Course(int courseId, String courseName, String courseDescription, int teacherId) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.teacherId = teacherId;
    }


    @Override
    public String toString() {
        return "Course{" +
                "id=" + courseId +
                ", userName='" + courseName + '\'' +
                ", password='" + courseDescription + '\'' +
                ", email='" + teacherId + '\'' +
                '}';
    }
}