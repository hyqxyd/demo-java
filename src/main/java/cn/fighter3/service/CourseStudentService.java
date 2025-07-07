package cn.fighter3.service;

import cn.fighter3.entity.CourseStudent;
import cn.fighter3.entity.ProblemStudent;
import cn.fighter3.entity.StudentAnswer;
import cn.fighter3.entity.User;
import cn.fighter3.mapper.CourseStudentMapper;
import cn.fighter3.mapper.ProblemMapper;
import cn.fighter3.mapper.StudentAnswerMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public
class CourseStudentService {

    @Autowired
    private CourseStudentMapper courseStudentMapper;
    @Autowired
    private  ProblemMapper problemMapper;
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Transactional
    public boolean batchAddStudents(List<CourseStudent> students) {
        // 1. 插入课程-学生关系
        int inserted = courseStudentMapper.insertBatch(students);
        if (inserted == 0) return false;

        // 2. 按课程分组学生
        Map<Integer, List<Integer>> courseToStudents = students.stream()
                .collect(Collectors.groupingBy(
                        CourseStudent::getCourseId,
                        Collectors.mapping(CourseStudent::getStudentId, Collectors.toList())
                ));

        // 3. 遍历处理每个课程
        for (Map.Entry<Integer, List<Integer>> entry : courseToStudents.entrySet()) {
            Integer courseId = entry.getKey();
            List<Integer> studentIds = entry.getValue();

            // 4. 获取课程下所有问题
            List<Integer> problemIds = problemMapper.getProblemIdsByCourseId(courseId);
            if (problemIds.isEmpty()) continue;

            // 5. 构建问题-学生关联和答案初始化数据
            List<ProblemStudent> problemStudents = new ArrayList<>();
            List<StudentAnswer> studentAnswers = new ArrayList<>();

            for (Integer problemId : problemIds) {
                for (Integer studentId : studentIds) {
                    // 5.1 构建问题-学生关联
                    problemStudents.add(new ProblemStudent(problemId, studentId, false));

                    // 5.2 构建初始答案记录
                    studentAnswers.add(new StudentAnswer() {{
                        setStudentId(studentId);
                        setProblemId(problemId);
                        // status 已在实体类设置默认值
                    }});
                }
            }

            // 6. 批量插入关联关系
            problemMapper.batchBindProblemToStudents(problemStudents);

            // 7. 批量初始化答案记录
            if (!studentAnswers.isEmpty()) {
                studentAnswerMapper.batchInsert(studentAnswers);
            }
        }

        return inserted > 0;
    }

    public List<User> getStudentsByCourseId(Integer courseId) {
        return courseStudentMapper.getStudentsByCourseId(courseId);
    }


    @Transactional
    public boolean removeStudent(CourseStudent student) {
        // 1. 删除课程-学生关联
        int deleted = courseStudentMapper.delete(student.getStudentId(), student.getCourseId());
        if (deleted == 0) return false;

        // 2. 获取课程下的所有问题ID
        List<Integer> problemIds = problemMapper.getProblemIdsByCourseId(student.getCourseId());

        // 3. 删除问题关联和答案记录（如果有问题）
        if (!problemIds.isEmpty()) {
            // 3.1 删除问题-学生关联
            problemMapper.deleteProblemStudentByCourse(student.getStudentId(), student.getCourseId());

            // 3.2 删除学生答案记录
            studentAnswerMapper.deleteByStudentAndProblems(
                    student.getStudentId(),
                    problemIds
            );
        }

        return true;
    }

    // --------------------- 批量删除学生 ---------------------
    @Transactional
    public boolean batchRemoveStudents(List<CourseStudent> students) {
        // 1. 批量删除课程-学生关联
        int deleted = courseStudentMapper.deleteBatch(students);
        if (deleted == 0) return false;

        // 2. 按课程分组学生
        Map<Integer, List<Integer>> courseToStudents = students.stream()
                .collect(Collectors.groupingBy(
                        CourseStudent::getCourseId,
                        Collectors.mapping(CourseStudent::getStudentId, Collectors.toList())
                ));

        // 3. 遍历处理每个课程
        List<ProblemStudent> toDeleteProblemLinks = new ArrayList<>();
        List<ProblemStudent> toDeleteAnswers = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : courseToStudents.entrySet()) {
            Integer courseId = entry.getKey();
            List<Integer> studentIds = entry.getValue();

            // 3.1 获取课程下的所有问题ID
            List<Integer> problemIds = problemMapper.getProblemIdsByCourseId(courseId);
            if (problemIds.isEmpty()) continue;

            // 3.2 构建待删除数据
            for (Integer studentId : studentIds) {
                // 构建问题-学生关联删除列表
                problemIds.forEach(problemId ->
                        toDeleteProblemLinks.add(new ProblemStudent(problemId, studentId, false))
                );

                // 构建学生答案删除列表
                problemIds.forEach(problemId ->
                        toDeleteAnswers.add(new ProblemStudent(problemId, studentId, false))
                );
            }
        }

        // 4. 批量删除操作
        if (!toDeleteProblemLinks.isEmpty()) {
            problemMapper.batchDeleteProblemStudent(toDeleteProblemLinks);
        }
        if (!toDeleteAnswers.isEmpty()) {
            studentAnswerMapper.batchDelete(toDeleteAnswers);
        }

        return deleted > 0;
    }

    // 根据角色查询用户

    public List<User> getUsersByRole(String role) {
        return courseStudentMapper.selectByRole(role);
    }
}