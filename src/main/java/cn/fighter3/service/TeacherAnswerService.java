package cn.fighter3.service;

import cn.fighter3.dto.ReviewDTO;
import cn.fighter3.dto.TeacherAnswerQueryDTO;
import cn.fighter3.vo.StudentAnswerReviewVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface TeacherAnswerService {
    IPage<StudentAnswerReviewVO> getStudentAnswers(TeacherAnswerQueryDTO dto);
    boolean reviewAnswer(ReviewDTO reviewDTO);
}
