package cn.fighter3.service.impl;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.dto.QueryIdDTO;
import cn.fighter3.entity.*;
import cn.fighter3.mapper.CourseMapper;
import cn.fighter3.mapper.ProblemMapper;
import cn.fighter3.mapper.TopicMapper;
import cn.fighter3.service.CourseService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private  ProblemMapper problemMapper;



    @Autowired
    private TopicMapper topicMapper;
    public List<Course> getAllCourses() {
        return courseMapper.getAllCourses();
    }

    @Override
    public void batchAddCourseStudent(int courseId,List<User> users){

        // 验证参数有效性
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("学生列表不能为空");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("课程ID无效");
        }

        for (User user : users) {
           System.out.println(user.toString());
        }



        // 提取用户ID列表
        List<Integer> userIds = users.stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        courseMapper.batchInsertCourseStudent(courseId, userIds);
        // 获取课程所有问题ID

        List<Problem> problems = problemMapper.selectProblemIdsByCourseId(courseId);



        List<ProblemStudent> binds = new ArrayList<>();
        problems.forEach(p ->
                users.forEach(u ->
                        binds.add(new ProblemStudent(p.getId(), u.getId(), false))
                )
        );


        // 批量插入前验证数据量（MySQL限制最大65535参数）
        if(!binds.isEmpty()){
            problemMapper.batchBindStudentsToProblems(binds);
        }

    }



    @Override
    public IPage<Course> selectCoursePage(QueryDTO queryDTO) {
        Page<Course> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        //System.out.println("我在哪？");
        //System.out.println(  courseMapper.selectCoursePage(page, queryDTO.getKeyword()));
        return courseMapper.selectCoursePage(page, queryDTO.getKeyword());
    }

    @Override
    public IPage<Course> selectByIdCoursePage(QueryIdDTO queryIdDTO) {
        Page<Course> page = new Page<>(queryIdDTO.getPageNo(), queryIdDTO.getPageSize());
        //System.out.println("我在哪？");
        //System.out.println(  courseMapper.selectCoursePage(page, queryDTO.getKeyword()));
        return courseMapper.selectByIdCoursePage(page, queryIdDTO.getTeacherId());
    }

    @Override
    public Course addCourse(Course course) {
        courseMapper.insert(course);
        return course ;
    }
    @Override
    public void addTeacherCourse(Course course) {

        courseMapper.insertTeacherCourse(course.getCourseId(),course.getTeacherId());

    }

    @Override
    public Integer updateCourse(Course course) {
        return courseMapper.updateById(course);
    }

    @Override
    public Integer deleteCourse(Integer id) {
        return courseMapper.deleteById(id);
    }

    @Override
    public  void deleteTeacherCourse(Integer id){
        System.out.println("删除课程子");
        courseMapper.deleteTeacherCourse(id);
    }




    @Override
    public void batchDeleteCourse(List<Integer> ids) {
        courseMapper.deleteBatchIds(ids);
    }

    @Override
    public void batchDeleteTeacherCourse(List<Integer> ids) {
        courseMapper.batchDeleteTeacherCourse(ids);
    }
    @Override
    public List<Course> getCoursesByStudentId(Integer studentId) {


      return courseMapper.selectCoursesByStudentId(studentId);
    }
    public List<Course> getCoursesByTeacherId(Integer teacherId) {


        return courseMapper.selectCoursesByTeacherId(teacherId);
    }
    public List<Topic> getTopicsByCourseId(Integer courseId) {
        return topicMapper.selectTopicsByCourseId(courseId);
    }
    @Override
    public Integer addTopic(Topic topic) {
        return topicMapper.insert(topic);
    }

    @Override
    public Integer updateTopic(Topic topic) {
        return topicMapper.updateById(topic);
    }

    @Override
    public Integer deleteTopic(Integer id) {
        return topicMapper.deleteById(id);
    }



}