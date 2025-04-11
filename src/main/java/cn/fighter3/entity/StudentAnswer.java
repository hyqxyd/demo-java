package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_answer")
public class StudentAnswer {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer problemId;
    private String status = "待提交";  // 默认值
    private String content;
    private String teacherFeedback;
    private BigDecimal score;
    private LocalDateTime updatedTime;
    // 其他字段根据业务需求可暂时忽略或设为null
}