package cn.fighter3.dto;

import cn.fighter3.entity.Answer;
import cn.fighter3.entity.Question;

import java.util.List;

public class QuestionDetail {
    private Question question;
    private List<Answer> answers;



    // Getters and Setters
    public Question getQuestion() {
        return this.question; }
    public void setQuestion(Question question) { this.question = question; }
    public List<Answer> getAnswers() { return this.answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers;
    }


}