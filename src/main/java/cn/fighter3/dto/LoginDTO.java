package cn.fighter3.dto;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description:
 **/

public class LoginDTO {
    private String loginName;
    private String password;
    private String role;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
