package cn.fighter3.mapper;

import cn.fighter3.entity.Course;
import cn.fighter3.entity.Course_student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMapper extends BaseMapper<Course> {
    List<Course> getAllCourses();
    IPage<Course> selectCoursePage(Page<Course> page, String keyword);

    @Select("SELECT student_id FROM courses_student WHERE course_id = #{courseId}")
    List<Integer> selectStudentIdsByCourseId(int courseId);


    @Insert({
            "<script>",
            "INSERT INTO courses_student (student_id, course_id) VALUES ",
            "<foreach collection='userIds' item='userId' separator=','>",
            "(#{userId}, #{courseId})",
            "</foreach>",
            "</script>"
    })
    void batchInsertCourseStudent(
            @Param("courseId") int courseId,
            @Param("userIds") List<Integer> userIds
    );



    @Delete("DELETE FROM teacher_course WHERE course_id = #{couresId} ")
    void deleteTeacherCourse(int couresId);

    /**
     * 批量删除 teacher_course 记录，根据 course_id
     */
    @Delete("<script>DELETE FROM teacher_course WHERE course_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    void batchDeleteTeacherCourse(List<Integer> ids);

    @Select("SELECT c.* FROM courses c JOIN teacher_course tc ON c.course_id = tc.course_id WHERE tc.teacher_id = #{teacherId} ")
    IPage<Course> selectByIdCoursePage(Page<Course> page, int teacherId);
    @Select("SELECT c.* FROM courses c JOIN courses_student cs ON c.course_id = cs.course_id WHERE cs.student_id = #{studentId}")
    List<Course> selectCoursesByStudentId(@Param("studentId") Integer studentId);


    @Select("SELECT c.* FROM courses c JOIN teacher_course tc ON c.course_id = tc.course_id WHERE tc.teacher_id = #{teacherId} ")
    List<Course> selectCoursesByTeacherId(@Param("teacherId") Integer teacherId);

    @Insert("INSERT INTO teacher_course(course_id,teacher_id) VALUES(#{courseId},#{teacherId})")
    int insertTeacherCourse(int courseId, int teacherId);
}