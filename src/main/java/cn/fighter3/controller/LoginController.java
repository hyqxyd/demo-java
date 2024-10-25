package cn.fighter3.controller;

import cn.fighter3.dto.LoginDTO;
import cn.fighter3.result.Result;
import cn.fighter3.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description: TODO
 **/
@RestController()
public class LoginController {
    @Autowired
    LoginService loginService;

    @PostMapping(value = "/api/login")
    public Result login(@RequestBody LoginDTO loginDTO){
      return loginService.login(loginDTO);
    }

    @PostMapping(value = "/api/hello")
    public Result hello(){
        return new Result(200,"hello","world");
    }
}
