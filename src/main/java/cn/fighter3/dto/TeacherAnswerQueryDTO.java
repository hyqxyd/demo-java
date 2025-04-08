package cn.fighter3.dto;

public class TeacherAnswerQueryDTO extends QueryDTO {
    private Integer pageNo;    //页码
    private Integer pageSize;  //页面大小
    private Integer teacherId;
    private Integer courseId;
    private Integer topicId;
    private Integer questionId;

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    @Override
    public Integer getPageNo() {
        return pageNo;
    }

    @Override
    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
}

