package cn.fighter3.controller;

import cn.fighter3.entity.CourseStudent;
import cn.fighter3.service.CourseStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course_student")
public class CourseStudentController {

    @Autowired
    private CourseStudentService courseStudentService;

    // 添加学生到课程
    @PostMapping("/add")
    public String addStudentToCourse(@RequestParam Integer studentId, @RequestParam Integer courseId) {
        boolean success = courseStudentService.addStudentToCourse(studentId, courseId);
        return success ? "学生成功添加到课程" : "添加学生到课程失败";
    }

    // 删除学生从课程中
    @DeleteMapping("/remove")
    public String removeStudentFromCourse(@RequestParam Integer studentId, @RequestParam Integer courseId) {
        boolean success = courseStudentService.removeStudentFromCourse(studentId, courseId);
        return success ? "学生已成功从课程中移除" : "从课程中移除学生失败";
    }

    // 获取课程中的所有学生
    @GetMapping("/students/{courseId}")
    public List<CourseStudent> getStudentsByCourse(@PathVariable Integer courseId) {
        return courseStudentService.getStudentsByCourse(courseId);
    }

    // 获取学生参加的所有课程
    @GetMapping("/courses/{studentId}")
    public List<CourseStudent> getCoursesByStudent(@PathVariable Integer studentId) {
        return courseStudentService.getCoursesByStudent(studentId);
    }

    // 更新学生的课程信息
    @PutMapping("/update")
    public String updateCourseForStudent(@RequestParam Integer studentId, @RequestParam Integer oldCourseId, @RequestParam Integer newCourseId) {
        boolean success = courseStudentService.updateCourseForStudent(studentId, oldCourseId, newCourseId);
        return success ? "学生课程更新成功" : "学生课程更新失败";
    }
}
