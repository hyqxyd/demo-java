package cn.fighter3.controller;

import java.util.List;
import java.util.Map;

import cn.fighter3.entity.CourseStudent;
import cn.fighter3.result.Result;
import cn.fighter3.service.CourseStudentService;
import cn.fighter3.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course")
public class CourseStudentController {

    @Autowired
    private CourseStudentService courseStudentService;

    // 获取课程学生列表
    @GetMapping("/{courseId}/students")
    public List<User> getCourseStudents(@PathVariable Integer courseId) {
        System.out.println("正在测试获取课程下学生");
        return courseStudentService.getStudentsByCourseId(courseId);
    }

    // 批量添加学生
    @PostMapping("/students/add")
    public Result batchAddStudents(@RequestBody List<CourseStudent> students) {
        boolean success = courseStudentService.batchAddStudents(students);
        if (success) {
            return new Result(200, "批量添加成功", null);
        } else {
            return new Result(400, "批量添加失败", null);
        }
    }

    // 移除单个学生
    @PostMapping("/student/remove")
    public Result removeStudentFromCourse(@RequestBody CourseStudent student) {
        boolean success = courseStudentService.removeStudent(student);
        if (success) {
            return new Result(200, "学生移除成功", null);
        } else {
            return new Result(400, "学生移除失败", null);
        }
    }

    //批量移除学生
    @PostMapping("/students/batch-remove")
    public Result batchRemoveStudents(@RequestBody List<CourseStudent> students) {
        boolean success = courseStudentService.batchRemoveStudents(students);
        if (success) {
            return new Result(200, "批量移除成功", null);
        } else {
            return new Result(400, "批量移除失败", null);
        }
    }
    // 根据角色查询用户
    @PostMapping("/studentList")
    public List<User> getUsersByRole(@RequestBody Map<String, String> params) {
        String role = params.get("role");
        return courseStudentService.getUsersByRole(role);
    }


}