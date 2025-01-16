package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("courses_student")
public class Course_student {
    @TableId(type = IdType.AUTO)
    private int studentId;
    private int courseId;



}
