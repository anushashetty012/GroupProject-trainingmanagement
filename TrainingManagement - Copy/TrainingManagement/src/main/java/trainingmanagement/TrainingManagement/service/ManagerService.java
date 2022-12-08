package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.entity.Course;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerService
{
    @Autowired
    private JdbcTemplate jdbcTemplate;
    int offset=0;

    //Get List of upcoming Active Completed courses assigned to a manager
    public Map<Integer,List<Course>> getAssignedCoursesForManagerByStatus(String empId, String completionStatus, int page, int limit)
    {
        Map map = new HashMap<Integer,List>();
        offset = limit *(page-1);
        String query = "SELECT Course.courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course,ManagersCourses WHERE Course.courseId = ManagersCourses.courseId and managerId=? and completionStatus=? and deleteStatus=false";
        List<Course> courses = jdbcTemplate.query(query,new BeanPropertyRowMapper<Course>(Course.class),empId,completionStatus);
        if (courses.size()!=0)
        {
            map.put(courses.size(),courses);
            return map;
        }
        return null;
    }

    public int getCourseCountForManagerByStatus(String managerId, String completionStatus)
    {
        String query = "select count(ManagersCourses.courseId) from ManagersCourses, Course where ManagersCourses.courseId = Course.courseId " +
                "and Course.completionStatus = ? and managerId = ? and Course.deleteStatus=false" ;
        return jdbcTemplate.queryForObject(query, Integer.class,completionStatus,managerId);
    }
}

