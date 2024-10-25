package cn.fighter3.service;

import cn.fighter3.dto.QuestionDetail;
import cn.fighter3.entity.Question;
import cn.fighter3.mapper.QuestionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class QuestionService{
    @Autowired
    private QuestionMapper questionMapper;


    public int saveQuestion(int userId, int courseId, String questionText) {
        Question question = new Question();
        question.setUserId(userId);
        question.setCourseId(courseId);
        question.setQuestionText(questionText);
        question.setQuestionTime(new Date()); // 设置当前时间
        questionMapper.insert(question); // 调用Mapper插入question
        return question.getQuestionId();
    }
}