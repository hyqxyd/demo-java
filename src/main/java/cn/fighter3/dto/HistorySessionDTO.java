package cn.fighter3.dto;

import java.util.Date;

public class HistorySessionDTO {
    private Date date;
    private String topic;
    private String content;

    public Date getDate() {
    return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;}

}
