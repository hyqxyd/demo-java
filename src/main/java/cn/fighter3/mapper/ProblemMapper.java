package cn.fighter3.mapper;

import cn.fighter3.entity.Problem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ProblemMapper extends BaseMapper<Problem> {



    @Select("select * from problem where topic_id=#{topicId}")
    List<Problem> getProblemsByTopicId(Integer topicId);

    @Insert("insert into problem_student (problem_id,student_id,learned) values (#{problemId},#{studentId},#{learned})")
    void insertProblemStudent(@Param("problemId")Integer problemId , @Param("studentId")Integer studentId,@Param("learned")Integer leaned);

    @Update("update problem_student set learned=1 where problem_id=#{problemId} and student_id=#{studentId}")
    void updateProblemStudent(@Param("problemId")Integer problemId , @Param("studentId")Integer studentId );

}
