// ModelDurationDTO.java
package cn.fighter3.dto;

import lombok.Data;

@Data
public class ModelDurationDTO {
    private Integer modelUsed;   // 模型ID
    private String modelName;    // 模型名称
    private Integer totalDuration; // 总时长（单位：秒）
}