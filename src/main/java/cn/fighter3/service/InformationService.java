package cn.fighter3.service;

import cn.fighter3.dto.QuestionDetail;

import java.util.List;

public interface InformationService {
    List<QuestionDetail> getUserQuestionsAndAnswers(int userId);
}
