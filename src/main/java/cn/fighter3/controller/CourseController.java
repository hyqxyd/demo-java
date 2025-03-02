package cn.fighter3.controller;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.Course;
import cn.fighter3.entity.Topic;
import cn.fighter3.result.Result;
import cn.fighter3.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @PostMapping("/list")
    public Result courseList(@RequestBody QueryDTO queryDTO){

        //System.out.println(courseService.selectCoursePage(queryDTO));

       // System.out.println("输出在哪？");
        System.out.println(new Result(200,"",courseService.selectCoursePage(queryDTO)).getData()+"输出");
        return new Result(200,"",courseService.selectCoursePage(queryDTO));
    }

    @PostMapping("/add")
    public Result addCourse(@RequestBody Course course){
        return new Result(200,"",courseService.addCourse(course));
    }

    @PostMapping("/update")
    public Result updateCourse(@RequestBody Course course){
        return new Result(200,"",courseService.updateCourse(course));
    }

    @PostMapping("/delete")
    public Result deleteCourse(@RequestParam Integer id){
        return new Result(200,"",courseService.deleteCourse(id));
    }

    @PostMapping("/delete/batch")
    public Result batchDeleteCourse(@RequestBody List<Integer> ids){
        courseService.batchDeleteCourse(ids);
        return new Result(200,"","");
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


}