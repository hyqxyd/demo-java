package cn.fighter3.controller;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.dto.QueryIdDTO;
import cn.fighter3.entity.Course;
import cn.fighter3.entity.Problem;
import cn.fighter3.entity.Topic;
import cn.fighter3.entity.User;
import cn.fighter3.result.Result;
import cn.fighter3.service.CourseService;
import cn.fighter3.service.ExcelService;
import cn.fighter3.service.ProblemService;
import cn.fighter3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProblemService problemService;
    @PostMapping("/import-students")
    public Result importStudentsToCourse(@RequestParam("file") MultipartFile file, @RequestParam("courseId") Integer courseId){
        try {
            List<User> users = excelService.importUsersFromExcel(file);
           // 将users分成两组：已存在的和新用户
            List<User> existingUsers = new ArrayList<>();
            List<User> newUsers = new ArrayList<>();
              for (User user : users) {
                User existingUser = userService.findByAccount(user.getAccount());
                if (existingUser != null) {
                    // 如果用户已存在，使用已存在用户的ID
                    user.setId(existingUser.getId());
                    existingUsers.add(user);
                } else {
                    newUsers.add(user);
                }
            }
             // 批量插入新用户
            if (!newUsers.isEmpty()) {
                userService.batchAddUsers(newUsers);
            }
             // 合并所有用户列表
            users.clear();
            users.addAll(existingUsers);
            users.addAll(newUsers);
            
            System.out.println(users.toString());
            courseService.batchAddCourseStudent(courseId, users);
            return new Result(200, "导入成功", users.size());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(500, "导入失败", null);
        }
    }


    @PostMapping("/list")
    public Result courseList(@RequestBody QueryDTO queryDTO){

        //System.out.println(courseService.selectCoursePage(queryDTO));

       // System.out.println("输出在哪？");
        System.out.println(new Result(200,"",courseService.selectCoursePage(queryDTO)).getData()+"输出");
        return new Result(200,"",courseService.selectCoursePage(queryDTO));
    }
    @PostMapping("/byteacherid")
    public Result getCourseById(@RequestBody QueryIdDTO queryIdDTO){
        return new Result(200,"",courseService.selectByIdCoursePage(queryIdDTO));
    }



    @PostMapping("/add")
    public Result addCourse(@RequestBody Course course){
        course=courseService.addCourse(course);

         System.out.println(course.getCourseId());
         courseService.addTeacherCourse(course);



        return new Result(200,"","添加成功");
    }

    @PostMapping("/update")
    public Result updateCourse(@RequestBody Course course){
        return new Result(200,"",courseService.updateCourse(course));
    }

    @PostMapping("/delete")
    public Result deleteCourse(@RequestParam Integer id){
        courseService.deleteTeacherCourse(id);//删除教师课程关联
        return new Result(200,"",courseService.deleteCourse(id));
    }

    @PostMapping("/delete/batch")
    public Result batchDeleteCourse(@RequestBody List<Integer> ids){

       courseService.batchDeleteTeacherCourse(ids);//删除教师课程关联
        courseService.batchDeleteCourse(ids);
        return new Result(200,"","删除成功");
    }

    @GetMapping("/byteacher")
    public Result getCoursesByTeacherId(@RequestParam("teacherId") Integer teacherId) {

        System.out.println("老师id："+teacherId);
        System.out.println("课程："+courseService.getCoursesByStudentId(teacherId));
        return new Result(200,"",courseService.getCoursesByTeacherId(teacherId));
    }
    @GetMapping("/coursesByStudent")
    public Result getCoursesByStudentId(@RequestParam("studentId") Integer studentId) {

        System.out.println("学生id："+studentId);
        System.out.println("课程："+courseService.getCoursesByStudentId(studentId));
        return new Result(200,"",courseService.getCoursesByStudentId(studentId));
    }

    @GetMapping("/topics")
    public Result getTopicsByCourseId(@RequestParam("courseId")  Integer courseId) {
        System.out.println(courseId);
        System.out.println(courseService.getTopicsByCourseId(courseId));
        return new Result(200,"",courseService.getTopicsByCourseId(courseId));
    }
    @PostMapping("/topic/add")
    public Result addTopic(@RequestBody Topic topic) {
        return new Result(200, "", courseService.addTopic(topic));
    }

    @PostMapping("/topic/update")
    public Result updateTopic(@RequestBody Topic topic) {
        return new Result(200, "", courseService.updateTopic(topic));
    }

    @PostMapping("/topic/delete/{id}")
    public Result deleteTopic(@PathVariable("id") Integer id) {
        return new Result(200, "", courseService.deleteTopic(id));
    }
    @GetMapping("/problems")
    public Result getProblemsByTopicId(@RequestParam("topicId")  Integer topicId) {
        System.out.println(topicId);
        System.out.println(problemService.getProblemsByTopicId(topicId));
        return new Result(200,"",problemService.getProblemsByTopicId(topicId));
    }
    @PostMapping("/problem/add")
    public Result addProblem(@RequestBody Problem problem) {
        return new Result(200, "", problemService.addProblem(problem));
    }

    @PostMapping("/problem/update")
    public Result updateProblem(@RequestBody Problem problem) {
        return new Result(200, "",problemService.updateProblem(problem));
    }

    @PostMapping("/problem/delete/{id}")
    public Result deleteProblem(@PathVariable("id") Integer id) {
        return new Result(200, "", problemService.deleteProblem(id));
    }





}