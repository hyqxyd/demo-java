package cn.fighter3.entity;

import java.util.Date;

public class LearningRecordUpdate {
    private Date sessionEndTime;
    private int duration;



    public Date getSessionEndTime() {
        return sessionEndTime;
    }
    public void setSessionEndTime(Date sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
    }
    public int getDuration() {
        return duration;

    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
}
