package cn.fighter3.mapper;

import cn.fighter3.dto.StudentWithLearnStatusDTO;
import cn.fighter3.entity.ProblemStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// ProblemStudentMapper.java
public interface ProblemStudentMapper extends BaseMapper<ProblemStudent> {
    List<StudentWithLearnStatusDTO> selectStudentsWithLearnStatus(@Param("problemId") Integer problemId);
}