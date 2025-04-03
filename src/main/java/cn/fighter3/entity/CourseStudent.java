package cn.fighter3.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class CourseStudent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer studentId;
    private Integer courseId;
}