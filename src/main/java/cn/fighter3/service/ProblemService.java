package cn.fighter3.service;

import cn.fighter3.entity.Problem;
import cn.fighter3.entity.ProblemStudent;
import cn.fighter3.entity.User;
import cn.fighter3.mapper.ProblemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProblemService {
    @Autowired
    private ProblemMapper problemMapper;
    public List<Problem> getProblemsByTopicId(int topicId) {
        return problemMapper.getProblemsByTopicId(topicId);
    }
    public void insertProblemStudent(Integer problemId, Integer studentId) {
        problemMapper.insertProblemStudent(problemId, studentId,0);
    }

    public void updateProblemStudent(Integer problemId, Integer studentId) {
        problemMapper.updateProblemStudent(problemId, studentId);
    }
    public Integer addProblem(Problem problem) {
      int courseId=problemMapper.getCourseIdByTopicId(problem.getTopicId());
      List<User> students=problemMapper.getStudentsByCourseId(courseId);

      problemMapper.insert(problem);


      int problemId=problem.getId();
        List<ProblemStudent> binds = new ArrayList<>();
        students.forEach(u ->
                binds.add(new ProblemStudent(problemId, u.getId(), false))
        );

       return problemMapper.batchBindProblemToStudents(binds);
    }
    public Integer updateProblem(Problem problem) {
       return problemMapper.updateById(problem);
    }
    public Integer deleteProblem(Integer id) {

        problemMapper.deleteProblemStudent(id);
       return problemMapper.deleteById(id);
    }
}
