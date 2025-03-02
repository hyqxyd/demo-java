package cn.fighter3.dto;

public class StudentWithLearnStatusDTO {
    private Integer studentId;
    private String userName;
    private String email;
    private Boolean learned; // 该学生对当前问题的学习状态


    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Boolean getLearned() {
        return learned;
    }

}
