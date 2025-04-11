package cn.fighter3.dto;

import lombok.Data;

@Data
public class StudentAnswerQueryDTO extends QueryDTO {
    private Integer pageNo;    //页码
    private Integer pageSize;  //页面大小
    private Integer studentId;
    private String status; // 可选字段：全部、待提交、待批阅、已完成

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
