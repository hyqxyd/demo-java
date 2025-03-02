package cn.fighter3.service;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.Course;
import cn.fighter3.entity.Course_student;
import cn.fighter3.entity.Topic;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface CourseService {

    List<Course> getAllCourses();
    IPage<Course> selectCoursePage(QueryDTO queryDTO);
    Integer addCourse(Course course);
    Integer updateCourse(Course course);
    Integer deleteCourse(Integer id);
    void batchDeleteCourse(List<Integer> ids);
    List<Course> getCoursesByStudentId(Integer studentId);
    List<Course> getCoursesByTeacherId(Integer teacherId);
    List<Topic> getTopicsByCourseId(Integer courseId) ;
    Integer addTopic(Topic topic);
    Integer updateTopic(Topic topic);
    Integer deleteTopic(Integer id);

}