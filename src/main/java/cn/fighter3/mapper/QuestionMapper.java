package cn.fighter3.mapper;

import cn.fighter3.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {
    List<Question> selectByUserId(int userId);



}