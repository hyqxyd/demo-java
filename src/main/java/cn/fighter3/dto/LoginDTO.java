package cn.fighter3.dto;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description:
 **/

public class LoginDTO {

    private String account;
    private String password;
    private String role;


    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
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
