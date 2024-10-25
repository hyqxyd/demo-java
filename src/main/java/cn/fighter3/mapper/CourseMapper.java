package cn.fighter3.mapper;

import cn.fighter3.entity.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface CourseMapper extends BaseMapper<Course> {
    IPage<Course> selectCoursePage(Page<Course> page, String keyword);
}