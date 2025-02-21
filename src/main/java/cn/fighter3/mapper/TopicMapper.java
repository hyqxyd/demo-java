package cn.fighter3.mapper;

import cn.fighter3.entity.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TopicMapper extends BaseMapper<Topic> {
    @Select("SELECT * FROM topics WHERE courses_id = #{courseId}")
    List<Topic> selectTopicsByCourseId(@Param("courseId") Integer courseId);

}
