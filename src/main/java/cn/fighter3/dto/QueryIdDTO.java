package cn.fighter3.dto;

public class QueryIdDTO {
    private Integer pageNo;    //页码
    private Integer pageSize;  //页面大小
    private int teacherId;    //关键字

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

    public int getTeacherId() {
        return teacherId;
    }

    public void setKeyword(int keyword) {
        this.teacherId = teacherId;
    }
}
