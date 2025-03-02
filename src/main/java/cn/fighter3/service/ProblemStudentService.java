package cn.fighter3.service;

import cn.fighter3.dto.StudentWithLearnStatusDTO;
import cn.fighter3.mapper.ProblemStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// ProblemStudentService.java
@Service
public class ProblemStudentService {
    @Autowired
    private ProblemStudentMapper problemStudentMapper;

    public List<StudentWithLearnStatusDTO> getStudentsByProblemId(Integer problemId) {
        return problemStudentMapper.selectStudentsWithLearnStatus(problemId);
    }
}