package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("courses_student")
public class CourseStudent {

    @TableId(type = IdType.AUTO) // 自增主键
    @TableField("student_id")
    private Integer studentId;

    @TableField("course_id")
    private Integer courseId;

    // Getters and Setters
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "CourseStudent{" +
                "studentId=" + studentId +
                ", courseId=" + courseId +
                '}';
    }
}
