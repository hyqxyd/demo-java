package cn.fighter3.service;

import cn.fighter3.dto.StudentAnswerDetailDTO;
import cn.fighter3.dto.StudentAnswerQueryDTO;
import cn.fighter3.entity.StudentAnswer;
import cn.fighter3.mapper.StudentAnswerMapper;
import cn.fighter3.vo.StudentAnswerVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentAnswerService {

    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    public IPage<StudentAnswer> queryAnswers(StudentAnswerQueryDTO dto) {
        Page<StudentAnswer> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        QueryWrapper<StudentAnswer> wrapper = new QueryWrapper<>();

        wrapper.eq("student_id", dto.getStudentId());
        if (dto.getStatus() != null && !dto.getStatus().equals("全部")) {
            wrapper.eq("status", dto.getStatus());
        }

        wrapper.orderByDesc("updated_time");

        return studentAnswerMapper.selectPage(page, wrapper);
    }

    public boolean submitAnswer(Integer id, String content) {
        // 查询学生的回答记录
        StudentAnswer existing = studentAnswerMapper.selectById(id);

        if (existing != null) {
            // 修改原答案
            existing.setContent(content);
            existing.setStatus("待批阅");
            existing.setUpdatedTime(LocalDateTime.now());
            studentAnswerMapper.updateById(existing);
            return true;
        }
        return false; // 如果没有找到记录，返回 false
    }

    public IPage<StudentAnswerVO> queryAnswerWithDetails(StudentAnswerQueryDTO dto) {
        Page<StudentAnswerVO> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        return studentAnswerMapper.selectAnswerWithDetails(page, dto);
    }
    public StudentAnswerDetailDTO getAnswerDetail(Integer id) {
        return studentAnswerMapper.selectDetailById(id);
    }

    public int countUnfinishedProblems(Integer studentId) {
        Long count = studentAnswerMapper.selectCount(
                new QueryWrapper<StudentAnswer>()
                        .eq("student_id", studentId)
                        .eq("status", "待提交") // 根据实际状态字段名调整
        );
        return count != null ? count.intValue() : 0;
    }
}
