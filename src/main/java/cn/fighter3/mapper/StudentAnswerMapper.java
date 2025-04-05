package cn.fighter3.mapper;

import cn.fighter3.dto.StudentAnswerDetailDTO;
import cn.fighter3.dto.StudentAnswerQueryDTO;
import cn.fighter3.entity.ProblemStudent;
import cn.fighter3.entity.StudentAnswer;
import cn.fighter3.vo.StudentAnswerVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

// StudentAnswerMapper.java
@Mapper
public interface StudentAnswerMapper extends BaseMapper<StudentAnswer> {
    @Insert("<script>" +
            "INSERT INTO student_answer (student_id, problem_id, status) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.studentId}, #{item.problemId}, #{item.status})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<StudentAnswer> list);
    /**
     * 根据学生和问题列表删除答案记录
     */
    @Delete("<script>" +
            "DELETE FROM student_answer " +
            "WHERE student_id = #{studentId} " +
            "AND problem_id IN " +
            "<foreach item='problemId' collection='problemIds' open='(' separator=',' close=')'>" +
            "#{problemId}" +
            "</foreach>" +
            "</script>")
    int deleteByStudentAndProblems(@Param("studentId") Integer studentId,
                                   @Param("problemIds") List<Integer> problemIds);

    /**
     * 批量删除学生答案记录
     */
    @Delete("<script>" +
            "DELETE FROM student_answer " +
            "WHERE (student_id, problem_id) IN " +
            "<foreach item='item' collection='list' open='(' separator=',' close=')'>" +
            "(#{item.studentId}, #{item.problemId})" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("list") List<ProblemStudent> list);

    // StudentAnswerMapper.java
    @Delete("DELETE FROM student_answer WHERE problem_id = #{problemId}")
    int deleteByProblemId(@Param("problemId") Integer problemId);

    IPage<StudentAnswerVO> selectAnswerWithDetails(Page<StudentAnswerVO> page, @Param("dto") StudentAnswerQueryDTO dto);
    StudentAnswerDetailDTO selectDetailById(@Param("id") Integer id);
}