package cn.fighter3.service;

import cn.fighter3.entity.Answer;
import cn.fighter3.mapper.AnswerMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AnswerService extends ServiceImpl<AnswerMapper, Answer> {
    @Autowired
    private AnswerMapper answerMapper;
    @Transactional
    public void saveAnswer(int questionId, String answerText, int modelId ){
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setAnswerText(answerText);
        answer.setModelId(modelId);
        answer.setAnswerTime(new Date());
        answerMapper.insert(answer);

    };
}