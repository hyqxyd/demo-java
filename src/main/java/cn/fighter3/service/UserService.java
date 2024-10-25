package cn.fighter3.service;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @Author 三分恶
 * @Date 2021/1/23
 * @Description
 */
public interface UserService {

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    IPage<User> selectUserPage(QueryDTO queryDTO);

    Integer addUser(User user);

    Integer updateUser(User user);

    Integer deleteUser(Integer id);

    void batchDelete(List<Integer> ids);

    void batchAddUsers(List<User> users);

}
