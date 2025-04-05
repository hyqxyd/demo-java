package cn.fighter3.service.impl;

import cn.fighter3.dto.LoginDTO;
import cn.fighter3.entity.User;
import cn.fighter3.mapper.UserMapper;
import cn.fighter3.result.Result;
import cn.fighter3.service.LoginService;
import cn.fighter3.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description:
 **/
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result login(LoginDTO loginDTO) {

        System.out.println("loginDTO:"+loginDTO.getAccount());
        //校验参数
        System.out.println(loginDTO.getAccount()+loginDTO.getPassword());
        if (StringUtils.isEmpty(loginDTO.getAccount())){
            return new Result(400,"账号不能为空","");
        }
        if (StringUtils.isEmpty(loginDTO.getPassword())){
            return new Result(400,"密码不能为空","");
        }
        //通过登录名查询用户
        QueryWrapper<User> wrapper = new QueryWrapper();
        wrapper.eq("BINARY account", loginDTO.getAccount());



        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return new Result(401, "用户账号不存在", "");
        }
        //System.out.println(uer.toString());
        //比较密码
       // String a=uer.getPassword();
        //System.out.println(a+"sadasdasdq");
        if (user!=null&&user.getPassword().equals(loginDTO.getPassword())&&user.getrole().equals(loginDTO.getRole())){
            LoginVO loginVO=new LoginVO();
            loginVO.setId(user.getId());
            //这里token直接用一个uuid
            //使用jwt的情况下，会生成一个jwt token,jwt token里会包含用户的信息
            loginVO.setToken(UUID.randomUUID().toString());
            loginVO.setUser(user);

            System.out.println(loginDTO.toString());
            return new Result(200,"",loginVO);
        }
        return new Result(401,"登录失败","");
    }
}
