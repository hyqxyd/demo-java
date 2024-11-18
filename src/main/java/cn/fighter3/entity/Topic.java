package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("topics")
public class Topic {
    @TableId(type = IdType.AUTO)
    private int topicid;
    private String topicName;
    private int coursesid;


    public int getTopicid() {
        return topicid;
    }
    public void setTopicid(int topicid) {
        this.topicid = topicid;
    }
    public String getTopicName() {
        return topicName;
    }
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    public int getCoursesid() {
        return coursesid;
    }
    public void setCoursesid(int coursesid) {
        this.coursesid = coursesid;
    }

}
