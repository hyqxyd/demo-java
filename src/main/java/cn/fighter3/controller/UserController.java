package cn.fighter3.controller;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.User;
import cn.fighter3.result.Result;
import cn.fighter3.service.ExcelService;
import cn.fighter3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author 三分恶
 * @Date 2021/1/23
 * @Description 用户管理
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ExcelService excelService;
    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    @PostMapping("/api/user/list")
    public Result userList(@RequestBody QueryDTO queryDTO){
        return new Result(200,"",userService.selectUserPage(queryDTO));
    }

    /**
     * 添加
     * @param user
     * @return
     */
    @PostMapping("/api/user/add")
    public Result addUser(@RequestBody User user){
        return new Result(200,"",userService.addUser(user));
    }

    /**
     * 更新
     * @param user
     * @return
     */
    @PostMapping("/api/user/update")
    public Result updateUser(@RequestBody User user){
        return new Result(200,"",userService.updateUser(user));
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @PostMapping("/api/user/delete")
    public Result deleteUser(Integer id){
        return new Result(200,"",userService.deleteUser(id));
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @PostMapping("/api/user/delete/batch")
    public Result batchDeleteUser(@RequestBody List<Integer> ids){
        userService.batchDelete(ids);
        return new Result(200,"","");
    }

    /**
     * 通过Excel导入用户
     * @param file
     * @return
     */
    @PostMapping("/api/user/import")
    public Result importUsersFromExcel(@RequestParam("file") MultipartFile file){
        try {
            List<User> users = excelService.importUsersFromExcel(file);
            // 这里可以添加批量插入数据库的逻辑
            userService.batchAddUsers(users);
            return new Result(200, "导入成功", users.size());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(500, "导入失败", null);
        }
    }
}
