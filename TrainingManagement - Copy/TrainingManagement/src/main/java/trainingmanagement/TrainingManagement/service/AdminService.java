package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.entity.Employee;
import trainingmanagement.TrainingManagement.entity.ManagersCourses;
import trainingmanagement.TrainingManagement.request.MultipleEmployeeRequest;
import trainingmanagement.TrainingManagement.response.CourseList;
import trainingmanagement.TrainingManagement.response.EmployeeDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AdminService
{
    private String TRAINING_COUNT = "SELECT COUNT(courseId) FROM Course WHERE completionStatus=? and deleteStatus=false";
    private String GET_COURSE = "SELECT courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course WHERE completionStatus=? and deleteStatus=false";
    private String CREATE_COURSE = "INSERT INTO Course(courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,meetingInfo) values(?,?,?,?,?,?,?,?,?)";


    //allocate project manager
    private String COURSES_TO_MANAGER = "SELECT courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course WHERE completionStatus='upcoming' and deleteStatus=false LIMIT ?,?";
    private String GET_MANAGERS = "SELECT Employee.emp_Id,emp_Name,designation FROM Employee, employee_role WHERE Employee.emp_id = employee_role.emp_id and employee_role.role_name='manager' AND Employee.delete_status=false LIMIT ?,?";
    private String ASSIGN_MANAGER = "INSERT INTO ManagersCourses(managerId,courseId) VALUES(?,?)";
    int offset=0;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public int activeCourse()
    {
        return jdbcTemplate.queryForObject(TRAINING_COUNT, new Object[]{"active"}, Integer.class);
    }

    public int upcomingCourse()
    {
        return jdbcTemplate.queryForObject(TRAINING_COUNT, new Object[]{"upcoming"}, Integer.class);
    }
    public int completedCourse()
    {
        return jdbcTemplate.queryForObject(TRAINING_COUNT, new Object[]{"completed"}, Integer.class);
    }
    public List<CourseList> getCourse(String completionStatus)
    {
        return jdbcTemplate.query(GET_COURSE,(rs, rowNum) -> {
            return new CourseList(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"),rs.getTime("duration"),rs.getTime("startTime"),rs.getTime("endTime"),rs.getString("completionStatus"));
        },completionStatus);
    }

    public String createCourse(Course course)
    {
        jdbcTemplate.update(CREATE_COURSE,course.getCourseName(),course.getTrainer(),course.getTrainingMode(),course.getStartDate(),course.getEndDate(),course.getDuration(),course.getStartTime(),course.getEndTime(),course.getMeetingInfo());
        return "Course created successfully";
    }

    //for inviting---change


    //to allocate managers to course
    public Map<Integer,List<CourseList>> getCourseToAssignManager(int page, int limit)
    {
        Map map = new HashMap<Integer,List>();
        offset = limit *(page-1);
        List<CourseList> courseList =  jdbcTemplate.query(COURSES_TO_MANAGER,(rs, rowNum) -> {
            return new CourseList(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"),rs.getTime("duration"),rs.getTime("startTime"),rs.getTime("endTime"),rs.getString("completionStatus"));
        },offset,limit);
        if (courseList.size()!=0)
        {
            map.put(courseList.size(),courseList);
            return map;
        }
        return null;
    }

    public Map<Integer,List<EmployeeDetail>> getManagers(int page, int limit)
    {
        Map map = new HashMap<Integer,List>();
        offset = limit *(page-1);
        List<EmployeeDetail> employeeDetails =  jdbcTemplate.query(GET_MANAGERS,(rs, rowNum) -> {
            return new EmployeeDetail(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"));
        },offset,limit);
        if (employeeDetails.size() != 0)
        {
            map.put(employeeDetails.size(),employeeDetails);
            return map;
        }
        return null;
    }

    public String assignCourseToManager(int courseId, List<MultipleEmployeeRequest> courseToManager)
    {
        int count=0;
        int noOfManagers = courseToManager.size();
        for (int i = 0; i < noOfManagers; i++)
        {
            String query = "select managerId from ManagersCourses where managerId=? and courseId=?";
            List<ManagersCourses> managerId = jdbcTemplate.query(query, (rs, rowNum) -> {
                return new ManagersCourses(rs.getString("managerId"));
            }, courseToManager.get(i).getEmpId(), courseId);
            if (managerId.size() == 0)
            {
                jdbcTemplate.update(ASSIGN_MANAGER, new Object[]{courseToManager.get(i).getEmpId(), courseId});
            }
            else
            {
                count++;
            }
            if (count==noOfManagers)
            {
                return "This course is already allocated to this manager";
            }
        }
        return "Course allocated successfully";
    }
}
