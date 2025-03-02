package cn.fighter3.mapper;

import cn.fighter3.entity.Course;
import cn.fighter3.entity.Course_student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMapper extends BaseMapper<Course> {
    List<Course> getAllCourses();
    IPage<Course> selectCoursePage(Page<Course> page, String keyword);
    @Select("SELECT c.* FROM courses c JOIN courses_student cs ON c.course_id = cs.course_id WHERE cs.student_id = #{studentId}")
    List<Course> selectCoursesByStudentId(@Param("studentId") Integer studentId);


    @Select("SELECT c.* FROM courses c JOIN teacher_course tc ON c.course_id = tc.course_id WHERE tc.teacher_id = #{teacherId} ")
    List<Course> selectCoursesByTeacherId(@Param("teacherId") Integer teacherId);
}