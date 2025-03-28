package cn.fighter3.service.impl;

import cn.fighter3.entity.CourseStudent;
import cn.fighter3.mapper.CourseStudentMapper;
import cn.fighter3.service.CourseStudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseStudentServiceImpl extends ServiceImpl<CourseStudentMapper, CourseStudent> implements CourseStudentService {

    @Autowired
    private CourseStudentMapper courseStudentMapper;

    @Override
    public boolean addStudentToCourse(Integer studentId, Integer courseId) {
        CourseStudent courseStudent = new CourseStudent();
        courseStudent.setStudentId(studentId);
        courseStudent.setCourseId(courseId);
        return save(courseStudent); // MyBatis-Plus的save方法会自动插入记录
    }

    @Override
    public boolean removeStudentFromCourse(Integer studentId, Integer courseId) {
        int rowsAffected = courseStudentMapper.deleteByStudentIdAndCourseId(studentId, courseId);
        return rowsAffected > 0;
    }

    @Override
    public List<CourseStudent> getStudentsByCourse(Integer courseId) {
        // 使用 MyBatis-Plus 的 selectList 来根据条件查询
        return courseStudentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                .eq("course_id", courseId));
    }

    @Override
    public List<CourseStudent> getCoursesByStudent(Integer studentId) {
        // 使用 MyBatis-Plus 的 selectList 来根据条件查询
        return courseStudentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                .eq("student_id", studentId));
    }

    @Override
    public boolean updateCourseForStudent(Integer studentId, Integer oldCourseId, Integer newCourseId) {
        // 删除旧的课程记录
        removeStudentFromCourse(studentId, oldCourseId);
        // 添加新的课程记录
        return addStudentToCourse(studentId, newCourseId);
    }
}
