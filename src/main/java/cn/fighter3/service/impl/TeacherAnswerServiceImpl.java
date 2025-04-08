package cn.fighter3.service.impl;

import cn.fighter3.dto.ReviewDTO;
import cn.fighter3.dto.TeacherAnswerQueryDTO;
import cn.fighter3.mapper.StudentAnswerMapper;
import cn.fighter3.service.TeacherAnswerService;
import cn.fighter3.vo.StudentAnswerReviewVO;
import cn.fighter3.entity.StudentAnswer;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherAnswerServiceImpl implements TeacherAnswerService {

    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Override
    public IPage<StudentAnswerReviewVO> getStudentAnswers(TeacherAnswerQueryDTO dto) {
        Page<StudentAnswerReviewVO> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        return studentAnswerMapper.selectStudentAnswers(page, dto);
    }

    @Override
    public boolean reviewAnswer(ReviewDTO reviewDTO) {
        StudentAnswer answer = new StudentAnswer();
        answer.setId(reviewDTO.getStudentAnswerId());
        answer.setTeacherFeedback(reviewDTO.getTeacherFeedback());

        if ("pass".equals(reviewDTO.getAction())) {
            answer.setStatus("已完成");
        } else if ("reject".equals(reviewDTO.getAction())) {
            answer.setStatus("待提交");
        }

        return studentAnswerMapper.updateById(answer) > 0;
    }
}
