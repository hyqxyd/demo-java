package cn.fighter3.service;

import cn.fighter3.entity.Problem;
import cn.fighter3.mapper.ProblemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {
    @Autowired
    private ProblemMapper problemMapper;
    public List<Problem> getProblemsByTopicId(int topicId) {
        return problemMapper.getProblemsByTopicId(topicId);
    }
    public void insertProblemStudent(Integer problemId, Integer studentId) {
        problemMapper.insertProblemStudent(problemId, studentId,1);
    }

    public void updateProblemStudent(Integer problemId, Integer studentId) {
        problemMapper.updateProblemStudent(problemId, studentId);
    }
}
