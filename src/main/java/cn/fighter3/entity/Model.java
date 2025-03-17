package cn.fighter3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("models")
public class Model {
    @TableId(type = IdType.AUTO)
    private int modelId;
    private String modelName;

    public int getModelId() {
        return modelId;
    }
    public void setModelId(int modelId) {
        this.modelId = modelId;
    }
    public String getModelName() {
        return modelName;
    }
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    // Getters and Setters
}