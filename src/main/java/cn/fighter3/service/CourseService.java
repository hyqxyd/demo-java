package cn.fighter3.service;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.Course;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface CourseService {
    IPage<Course> selectCoursePage(QueryDTO queryDTO);
    Integer addCourse(Course course);
    Integer updateCourse(Course course);
    Integer deleteCourse(Integer id);
    void batchDeleteCourse(List<Integer> ids);
}