package cn.fighter3.dto;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description:
 **/

public class LoginDTO {

    private String id;
    private String password;
    private String role;

   public String getId() {
       return id;
   }

    public void setId(String Id) {
        this.id = Id;
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
