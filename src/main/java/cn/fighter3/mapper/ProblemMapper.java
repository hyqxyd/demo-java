package cn.fighter3.mapper;

import cn.fighter3.entity.Problem;
import cn.fighter3.entity.ProblemStudent;
import cn.fighter3.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ProblemMapper extends BaseMapper<Problem> {

    @Select("SELECT p.id AS id,p.problem,p.topic_id AS topicId FROM problem p " +
            "INNER JOIN topics t ON p.topic_id = t.topic_id " +
            "WHERE t.courses_id = #{courseId}")
    List<Problem> selectProblemIdsByCourseId(@Param("courseId") int courseId);

    @Select("SELECT t.courses_id FROM problem p " +
            "JOIN topics t ON p.topic_id = t.topic_id " +
            "WHERE p.id = #{problemId}")
    Integer selectCourseIdByProblemId(int problemId);




    //根据topicid获取课程id
    @Select("select courses_id from topics where topic_id=#{topicId}")
    Integer getCourseIdByTopicId(@Param("topicId") Integer topicId);

    //根据课程id获取学生
    @Select("select  u.id, u.user_name, u.email, u.role" +
            " from user u " +
            " INNER JOIN courses_student cs ON u.id = cs.student_id" +
            " where cs.course_id=#{courseId}")
    List<User> getStudentsByCourseId(@Param("courseId") Integer courseId);
    //删除绑定关系
    @Delete("delete from problem_student where problem_id=#{problemId}")
    void deleteProblemStudent(@Param("problemId")Integer problemId);




    void batchBindStudentsToProblems(@Param("list") List<ProblemStudent> binds);


    int batchBindProblemToStudents(
            @Param("list") List<ProblemStudent> binds
    );

    @Select("select * from problem where topic_id=#{topicId}")
    List<Problem> getProblemsByTopicId(Integer topicId);

    @Insert("insert into problem_student (problem_id,student_id,learned) values (#{problemId},#{studentId},#{learned})")
    void insertProblemStudent(@Param("problemId")Integer problemId , @Param("studentId")Integer studentId,@Param("learned")Integer leaned);

    @Update("update problem_student set learned=1 where problem_id=#{problemId} and student_id=#{studentId}")
    void updateProblemStudent(@Param("problemId")Integer problemId , @Param("studentId")Integer studentId );

}
