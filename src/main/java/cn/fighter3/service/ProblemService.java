package cn.fighter3.service;

import cn.fighter3.entity.Problem;
import cn.fighter3.entity.ProblemStudent;
import cn.fighter3.entity.StudentAnswer;
import cn.fighter3.entity.User;
import cn.fighter3.mapper.ProblemMapper;
import cn.fighter3.mapper.StudentAnswerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProblemService {
    @Autowired
    private ProblemMapper problemMapper;
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
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
        // 1. 插入问题
        problemMapper.insert(problem);
        int problemId = problem.getId();

        // 2. 获取关联课程的学生列表
        int courseId = problemMapper.getCourseIdByTopicId(problem.getTopicId());
        List<User> students = problemMapper.getStudentsByCourseId(courseId);

        // 3. 初始化关联关系（当有学生时）
        if (students != null && !students.isEmpty()) {
            // 3.1 构建问题-学生关联列表
            List<ProblemStudent> problemStudents = students.stream()
                    .map(u -> new ProblemStudent(problemId, u.getId(), false))
                    .collect(Collectors.toList());

            // 3.2 构建学生答案初始化列表
            List<StudentAnswer> studentAnswers = students.stream()
                    .map(u -> new StudentAnswer() {{
                        setStudentId(u.getId());
                        setProblemId(problemId);
                        setStatus("待提交");  // 与数据库默认值保持一致
                    }})
                    .collect(Collectors.toList());

            // 3.3 批量插入
            problemMapper.batchBindProblemToStudents(problemStudents);
            studentAnswerMapper.batchInsert(studentAnswers);
        }

        return problemId;
    }
    public Integer updateProblem(Problem problem) {
       return problemMapper.updateById(problem);
    }
    public Integer deleteProblem(Integer id) {
        // 1. 删除问题-学生关联
        problemMapper.deleteProblemStudent(id);

        // 2. 删除学生答案记录（新增）
        studentAnswerMapper.deleteByProblemId(id);

        // 3. 删除问题主体
        return problemMapper.deleteById(id);
    }
}
