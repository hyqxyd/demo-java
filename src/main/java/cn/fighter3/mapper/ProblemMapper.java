package cn.fighter3.mapper;

import cn.fighter3.entity.Problem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProblemMapper extends BaseMapper<Problem> {


    @Select("select * from problem where topic_id=#{topicId}")
    List<Problem> getProblemsByTopicId(Integer topicId);


}
