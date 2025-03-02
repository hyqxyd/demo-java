// DurationDTO.java
package cn.fighter3.dto;

import lombok.Data;

@Data
public class DurationDTO {
    private String date;
    private Integer totalDuration;

    public DurationDTO(String date, Integer totalDuration) {
        this.date = date;
        this.totalDuration = totalDuration;

    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public Integer getTotalDuration() {
        return totalDuration;

    }
    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }


}