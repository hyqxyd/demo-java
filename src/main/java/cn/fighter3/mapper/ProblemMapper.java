package cn.fighter3.mapper;

import cn.fighter3.entity.Problem;
import cn.fighter3.entity.ProblemStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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


    void batchBindStudentsToProblems(@Param("list") List<ProblemStudent> binds);

    // 批量插入问题-学生关联
    @Insert({
            "<script>",
            "INSERT IGNORE INTO problem_student (problem_id, student_id, learned)",
            "VALUES ",
            "<foreach collection='studentIds' item='studentId' separator=','>",
            "(#{problemId}, #{studentId}, DEFAULT)",
            "</foreach>",
            "</script>"
    })
    int batchBindProblemToStudents(
            @Param("problemId") int problemId,
            @Param("studentIds") List<Integer> studentIds
    );

    @Select("select * from problem where topic_id=#{topicId}")
    List<Problem> getProblemsByTopicId(Integer topicId);

    @Insert("insert into problem_student (problem_id,student_id,learned) values (#{problemId},#{studentId},#{learned})")
    void insertProblemStudent(@Param("problemId")Integer problemId , @Param("studentId")Integer studentId,@Param("learned")Integer leaned);

    @Update("update problem_student set learned=1 where problem_id=#{problemId} and student_id=#{studentId}")
    void updateProblemStudent(@Param("problemId")Integer problemId , @Param("studentId")Integer studentId );

}
