package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;
@TableName("learning_records")
public class LearningRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer problemId;
    private Date sessionStartTime;
    private Date sessionEndTime;

    private int modelUsed;
    private Integer duration;
    private String content;
    private String keywords;

    public Integer getId() {
        return this.id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getStudentId() {
        return this.studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public Integer getProblemId() {
        return this.problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }
    public Date getSessionStartTime() {
        return this.sessionStartTime;
    }
public void setSessionStartTime(Date sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
}

public Date getSessionEndTime() {
        return this.sessionEndTime;
}

public void setSessionEndTime(Date sessionEndTime) {

        this.sessionEndTime = sessionEndTime;
}
    public int getModelUsed() {
        return this.modelUsed;
    }
    public void setModelUsed(int modelUsed) {
        this.modelUsed = modelUsed;
    }
    public Integer getDuration() {
        return this.duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
public String getContent() {
        return this.content;
}

    public void setContent(String content) {
        this.content = content;
    }
    public String getKeywords() {
    return this.keywords;
}
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }



}
