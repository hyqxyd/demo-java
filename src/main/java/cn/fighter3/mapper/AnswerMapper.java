package cn.fighter3.mapper;

import cn.fighter3.entity.Answer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface AnswerMapper extends BaseMapper<Answer> {
     List<Answer> selectByQuestionId(int questionId);


}