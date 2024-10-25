package cn.fighter3.mapper;

import cn.fighter3.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class UserMapperTest {
    @Autowired
    UserMapper userMapper;

    @Test
    @DisplayName("插入数据")
    public void testInsert(){
        User user=new User("test1","test","t123","男","test1@qq.com","满都镇");
        Integer id=userMapper.insert(user);
        System.out.printf(id.toString());
    }

    @Test
    @DisplayName("根据id查找")
    public void testSelectById(){
        User user=userMapper.selectById(1);
        System.out.println(user.toString());
    }

    @Test
    @DisplayName("查找所有")
    public void testSelectAll(){
        List userList=userMapper.selectObjs(null);
        System.out.println(userList.size());
    }

    @Test
    @DisplayName("更新")
    public void testUpdate(){
        User user=new User();
        user.setId(1);
        user.setAddress("金葫芦镇");
        Integer id=userMapper.updateById(user);
        System.out.println(id);
    }

    @Test
    @DisplayName("删除")
    public void testDelete(){
        userMapper.deleteById(1);
    }

    @Test
    @DisplayName("Wrapper查询")
    public void selectWrapper(){
        QueryWrapper<User> wrapper = new QueryWrapper();
        wrapper.eq("login_name", "test1");
        User uer=userMapper.selectOne(wrapper);
        System.out.println(uer.toString());
    }

    @Test
    @DisplayName("分页查询")
    public void testSelectUserPages(){
        Integer pageNo=0;
        Integer pageSize=4;
        Page<User> page=new Page<>(pageNo,pageSize);
        //IPage<User> userIPage=userMapper.selectUserPage(page,"");
        IPage<User> userIPage=userMapper.selectUserPage(page,"张三");
        System.out.println(userIPage.getPages());
    }
}