package cn.fighter3.service;

import cn.fighter3.entity.CourseStudent;

import java.util.List;

public interface CourseStudentService {

    // 添加学生到课程
    boolean addStudentToCourse(Integer studentId, Integer courseId);

    // 删除学生从课程中
    boolean removeStudentFromCourse(Integer studentId, Integer courseId);

    // 获取某课程下的所有学生
    List<CourseStudent> getStudentsByCourse(Integer courseId);

    // 获取某学生的所有课程
    List<CourseStudent> getCoursesByStudent(Integer studentId);

    // 更新学生的课程信息（如果有必要）
    boolean updateCourseForStudent(Integer studentId, Integer oldCourseId, Integer newCourseId);
}
