package cn.fighter3.mapper;

import cn.fighter3.entity.CourseStudent;
import cn.fighter3.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public
interface CourseStudentMapper extends BaseMapper<CourseStudent> {
    @Select("SELECT * FROM user WHERE id IN (SELECT student_id FROM courses_student WHERE course_id = #{courseId})")
    List<User> getStudentsByCourseId(@Param("courseId") Integer courseId);

    @Insert("<script>" +
            "INSERT INTO courses_student (student_id, course_id) VALUES " +
            "<foreach collection='students' item='item' separator=','>" +
            "(#{item.studentId}, #{item.courseId})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("students") List<CourseStudent> students);


    @Delete("DELETE FROM courses_student WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int delete(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);

    @Delete("<script>" +
            "DELETE FROM courses_student WHERE (student_id, course_id) IN " +
            "<foreach item='item' collection='students' open='(' separator=',' close=')'>" +
            "(#{item.studentId}, #{item.courseId})" +
            "</foreach>" +
            "</script>")
    int deleteBatch(@Param("students") List<CourseStudent> students);

    @Select("select * from user where role = #{role}")
    List<User> selectByRole(String role);

}