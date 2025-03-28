package cn.fighter3.service.impl;

import cn.fighter3.dto.QueryDTO;
import cn.fighter3.entity.Course;
import cn.fighter3.entity.Topic;
import cn.fighter3.mapper.CourseMapper;
import cn.fighter3.mapper.TopicMapper;
import cn.fighter3.service.CourseService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseMapper courseMapper;


    @Autowired
    private TopicMapper topicMapper;
    public List<Course> getAllCourses() {
        return courseMapper.getAllCourses();
    }

    @Override
    public IPage<Course> selectCoursePage(QueryDTO queryDTO) {
        Page<Course> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        //System.out.println("我在哪？");
        System.out.println("这里是测试数据@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@queryDTO.getTeacherId()===="+queryDTO.getTeacherId());
        IPage<Course> result = courseMapper.selectCoursePage(page, queryDTO.getKeyword());
        System.out.println(result);
        return result;
    }

    @Override
    public Integer addCourse(Course course) {
        return courseMapper.insert(course);
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
    public void batchDeleteCourse(List<Integer> ids) {
        courseMapper.deleteBatchIds(ids);
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