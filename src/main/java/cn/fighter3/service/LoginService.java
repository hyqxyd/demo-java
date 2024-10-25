package cn.fighter3.service;

import cn.fighter3.dto.LoginDTO;
import cn.fighter3.result.Result;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description:
 **/

public interface LoginService {
    public Result login(LoginDTO loginDTO);
}
