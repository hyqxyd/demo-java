package cn.fighter3.mapper;

import cn.fighter3.entity.Question;
import cn.fighter3.entity.Session;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SessionMapper extends BaseMapper<Session> {

    List<Session> selectByUserId(@Param("userId") int userId);
    String selectNameByTId(@Param("tId") int tId);


    @Select("select * from sessions where user_id=#{studentId} and t_id=#{topicId} and p_id=#{problemId}")
    List<Session> selectBystudentIdtopicIdproblemId(@Param("studentId") int studentId,@Param("topicId") int topicId,@Param("problemId") int problemId);

}
