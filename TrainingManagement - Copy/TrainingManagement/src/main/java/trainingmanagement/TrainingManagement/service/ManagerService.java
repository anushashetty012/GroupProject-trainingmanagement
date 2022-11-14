package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.entity.Course;


import java.util.List;

@Service
public class ManagerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    //Omkar

    //Get List of upcoming Active Completed courses assigned to a manager
    public List<Course> getAssignedCoursesForManagerByStatus(String empId,String completionStatus)
    {
        String query = "SELECT course.courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course,managerscourses WHERE course.courseId = managerscourses.courseID and managerID=? and completionStatus=? and deleteStatus=false";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<Course>(Course.class),empId,completionStatus);
    }
}

