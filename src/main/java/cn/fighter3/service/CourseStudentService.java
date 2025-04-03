package cn.fighter3.service;

import cn.fighter3.entity.CourseStudent;
import cn.fighter3.entity.User;
import cn.fighter3.mapper.CourseStudentMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public
class CourseStudentService {

    @Autowired
    private CourseStudentMapper courseStudentMapper;

    public List<User> getStudentsByCourseId(Integer courseId) {
        return courseStudentMapper.getStudentsByCourseId(courseId);
    }

    public boolean batchAddStudents(List<CourseStudent> students) {
        return courseStudentMapper.insertBatch(students) > 0;
    }

    public boolean removeStudent(CourseStudent student) {
        // 正确：提取对象中的属性作为参数
        return courseStudentMapper.delete(student.getStudentId(), student.getCourseId()) > 0;
    }

    public boolean batchRemoveStudents(List<CourseStudent> students) {
        return courseStudentMapper.deleteBatch(students) > 0;
    }

    // 根据角色查询用户

    public List<User> getUsersByRole(String role) {
        return courseStudentMapper.selectByRole(role);
    }
}