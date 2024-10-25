package cn.fighter3.service.impl;

import cn.fighter3.dto.QuestionDetail;
import cn.fighter3.entity.Answer;
import cn.fighter3.entity.Question;
import cn.fighter3.mapper.AnswerMapper;
import cn.fighter3.mapper.QuestionMapper;
import cn.fighter3.service.InformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class InformationServiceImpl implements InformationService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AnswerMapper answerMapper;

    @Override
    public List<QuestionDetail> getUserQuestionsAndAnswers(int userId) {
        List<Question> questions = questionMapper.selectByUserId(userId);
        List<QuestionDetail> questionDetails = new ArrayList<>();
        for (Question question : questions) {
            QuestionDetail detail = new QuestionDetail();
            detail.setQuestion(question);
            List<Answer> answers = answerMapper.selectByQuestionId(question.getQuestionId());
            detail.setAnswers(answers);
            questionDetails.add(detail);
        }
        return questionDetails;
    }
}




