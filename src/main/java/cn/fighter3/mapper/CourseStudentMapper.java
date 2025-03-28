package cn.fighter3.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.fighter3.entity.CourseStudent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface CourseStudentMapper extends BaseMapper<CourseStudent> {

    // 根据学生ID和课程ID删除记录
    @Delete("DELETE FROM courses_student WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int deleteByStudentIdAndCourseId(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);

}
