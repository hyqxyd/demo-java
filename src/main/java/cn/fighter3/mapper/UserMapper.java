package cn.fighter3.mapper;

import cn.fighter3.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @Author: 三分恶
 * @Date: 2021/1/17
 * @Description: TODO
 **/

public interface UserMapper extends BaseMapper<User> {
    IPage<User> selectUserPage(Page<User> page,String keyword);


    void batchInsertUsers(List<User> users);
}
