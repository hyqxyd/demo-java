package cn.fighter3.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.persistence.Column;
import java.util.Date;


@TableName("sessions")
public class Session {
    private String id;
    private int qId;
    private int aId;
    private int mId;
    private int tId;
    @Column(columnDefinition = "TEXT")
    private String content;
    private int userId;
    private Date SessionTime;
    private  int pId;

    public int getPId() {
        return pId;
    }
    public void setPId(int pId) {
        this.pId = pId;
    }


    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getId() {
        return id;

    }

    public void setId(String id) {
        this.id = id;
    }
    public  int getQId() {
        return qId;}
    public void setQId(int qId) {
        this.qId = qId;
    }
    public int getAId() {
        return aId;
    }
    public void setAId(int aId) {
        this.aId = aId;
    }
    public int getMId() {
        return mId;
    }
    public void setMId(int mId) {
        this.mId = mId;
    }
    public int getTId() {
        return tId;
    }
    public void setTId(int tId) {
        this.tId = tId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Date getSessionTime() {
        return SessionTime;
    }
    public void setSessionTime() {
        SessionTime = new Date();
    }
}
