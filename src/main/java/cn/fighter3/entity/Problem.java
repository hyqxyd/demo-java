package cn.fighter3.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("problem")
public class Problem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String problem;
    private Integer topicId;


    public Integer getId() {
        return this.id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getProblem() {
        return this.problem;
    }
    public void setProblem(String problem) {
        this.problem = problem;
    }
    public Integer getTopicId() {
        return this.topicId;
    }
    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }



}
