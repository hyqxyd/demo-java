package cn.fighter3.mapper;

import cn.fighter3.dto.*;
import cn.fighter3.entity.LearningRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LearningRecordMapper extends BaseMapper<LearningRecord> {
    List<LearningRecord> getLearningRecordsByProblemId(Integer problemId);
    List<LearningRecord> getLearningRecordsByStudentAndProblem(Integer studentId, Integer problemId);
    // LearningRecordMapper.java
    List<FrequencyDTO> getDailyFrequency(@Param("studentId") Integer studentId,
                                         @Param("problemId") Integer problemId);

    List<ModelUsageDTO> getModelUsageCount(@Param("studentId") Integer studentId,
                                           @Param("problemId") Integer problemId);

    List<DurationDTO> getDailyDuration(@Param("studentId") Integer studentId,
                                       @Param("problemId") Integer problemId);
    // LearningRecordMapper.java
    List<ModelDurationDTO> getModelDuration(@Param("studentId") Integer studentId,
                                            @Param("problemId") Integer problemId);

    @Select("SELECT keywords FROM learning_records " +
            "WHERE student_id = #{studentId} AND problem_id = #{problemId} " +
            "AND keywords IS NOT NULL AND keywords != ''")
    List<String> findKeywordsByStudentAndProblem(
            @Param("studentId") Integer studentId,
            @Param("problemId") Integer problemId
    );

}
