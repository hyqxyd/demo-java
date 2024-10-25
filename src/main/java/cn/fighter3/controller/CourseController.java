package cn.fighter3.controller;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.Course;
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
    public Result deleteCourse(@RequestBody Integer id){
        return new Result(200,"",courseService.deleteCourse(id));
    }

    @PostMapping("/delete/batch")
    public Result batchDeleteCourse(@RequestBody List<Integer> ids){
        courseService.batchDeleteCourse(ids);
        return new Result(200,"","");
    }
}