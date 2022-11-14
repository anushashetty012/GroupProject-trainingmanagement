package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.entity.Invites;
import trainingmanagement.TrainingManagement.entity.ManagersCourses;
import trainingmanagement.TrainingManagement.request.FilterByDate;
import trainingmanagement.TrainingManagement.response.*;

import java.util.List;

@Service
public class EmployeeService
{
    @Autowired
    JdbcTemplate jdbcTemplate;
    private String GET_ACCEPTED_COUNT = "SELECT COUNT(empId) FROM AcceptedInvites WHERE courseId=? and deleteStatus=false";
    //for admin
    private String VIEW_COURSE_DETAILS = "SELECT courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus,meetingInfo FROM Course WHERE courseId=? and deleteStatus=false";
    //for manager
    private String CHECK_COURSE_ALLOCATION = "SELECT courseId FROM ManagersCourses WHERE managerId=? AND courseId=?";
    private String CHECK_IF_INVITED = "SELECT courseId FROM Invites where empId=? AND courseId=? and (acceptanceStatus=true or acceptanceStatus is null)";
    private String GET_ROLE = "SELECT role_name FROM employee_role WHERE emp_id=?";
    //to get role
    public String getRole(String empId)
    {
        return jdbcTemplate.queryForObject(GET_ROLE,new Object[]{empId},String.class);
    }
    public int getAcceptedCount(int courseId,String empId)
    {
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            return jdbcTemplate.queryForObject(GET_ACCEPTED_COUNT, new Object[]{courseId},Integer.class);
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            List<Invites> isInvited = jdbcTemplate.query(CHECK_IF_INVITED,(rs, rowNum) -> {
                return new Invites(rs.getInt("courseId"));
            },empId,courseId);

            List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION,(rs, rowNum) -> {
                return new ManagersCourses(rs.getInt("courseId"));
            },empId,courseId);
            if (isCourseAssigned.size()!=0 || isInvited.size()!=0)
            {
                return jdbcTemplate.queryForObject(GET_ACCEPTED_COUNT, new Object[]{courseId},Integer.class);
            }
        }
        if (getRole((empId)).equalsIgnoreCase("employee"))
        {
            List<Invites> isInvited = jdbcTemplate.query(CHECK_IF_INVITED,(rs, rowNum) -> {
                return new Invites(rs.getInt("courseId"));
            },empId,courseId);
            if (isInvited.size()!=0)
            {
                return jdbcTemplate.queryForObject(GET_ACCEPTED_COUNT, new Object[]{courseId},Integer.class);
            }
        }
        return 0;
    }
    public CourseInfo viewCourseDetails(int courseId, String empId)
    {
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            try
            {
                return jdbcTemplate.queryForObject(VIEW_COURSE_DETAILS,(rs, rowNum) -> {
                    return new CourseInfo(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"),rs.getTime("duration"),rs.getTime("startTime"),rs.getTime("endTime"),rs.getString("completionStatus"),rs.getString("meetingInfo"));
                },courseId);
            }
            catch (DataAccessException e)
            {
                return null;
            }
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            List<Invites> isInvited = jdbcTemplate.query(CHECK_IF_INVITED,(rs, rowNum) -> {
                return new Invites(rs.getInt("courseId"));
            },empId,courseId);

            List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION,(rs, rowNum) -> {
                return new ManagersCourses(rs.getInt("courseId"));
            },empId,courseId);
            if (isCourseAssigned.size()!=0 || isInvited.size()!=0)
            {
                return jdbcTemplate.queryForObject(VIEW_COURSE_DETAILS,(rs, rowNum) -> {
                    return new CourseInfo(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"),rs.getTime("duration"),rs.getTime("startTime"),rs.getTime("endTime"),rs.getString("completionStatus"),rs.getString("meetingInfo"));
                },courseId);
            }
        }
        if (getRole((empId)).equalsIgnoreCase("employee"))
        {
            try
            {
                List<Invites> isInvited = jdbcTemplate.query(CHECK_IF_INVITED,(rs, rowNum) -> {
                    return new Invites(rs.getInt("courseId"));
                },empId,courseId);
                if (isInvited.size()!=0)
                {
                    return jdbcTemplate.queryForObject(VIEW_COURSE_DETAILS,(rs, rowNum) -> {
                        return new CourseInfo(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"),rs.getTime("duration"),rs.getTime("startTime"),rs.getTime("endTime"),rs.getString("completionStatus"),rs.getString("meetingInfo"));
                    },courseId);
                }
            }
            catch (DataAccessException e)
            {
                return null;
            }
        }
        return null;
    }

    public String acceptInvite(int inviteId)
    {
        try
        {
            jdbcTemplate.update("update Invites set acceptanceStatus=true where inviteId=?",inviteId);
            AcceptedOrRejectedResponse invite = jdbcTemplate.queryForObject("select inviteId,empId,courseId from invites where inviteId=?",(rs, rowNum) -> {
                return new AcceptedOrRejectedResponse(rs.getInt("inviteId"),rs.getString("empId"),rs.getInt("courseId"));
            },inviteId);
            jdbcTemplate.update("insert into AcceptedInvites(inviteId,courseId,empId) values(?,?,?)",invite.getInviteId(),invite.getCourseId(),invite.getEmpId());
        }
        catch (DataAccessException e)
        {
            return null;
        }
        return "Accepted invite successfully";
    }
    public String rejectInvite(int inviteId, RejectedResponse reason)
    {
        try
        {
            jdbcTemplate.update("update Invites set acceptanceStatus=false where inviteId=?",inviteId);
            AcceptedOrRejectedResponse invite = jdbcTemplate.queryForObject("select inviteId,empId,courseId from invites where inviteId=?",(rs, rowNum) -> {
                return new AcceptedOrRejectedResponse(rs.getInt("inviteId"),rs.getString("empId"),rs.getInt("courseId"));
            },inviteId);
            jdbcTemplate.update("insert into RejectedInvites(inviteId,courseId,empId,reason) values(?,?,?,?)",invite.getInviteId(),invite.getCourseId(),invite.getEmpId(),reason.getReason());
        }
        catch (DataAccessException e)
        {
            return null;
        }
        return "Rejected invite successfully";
    }
    //attended and non attended course
    //profile info
    public EmployeeProfile profileInfo(String empId)
    {
        try
        {
            return jdbcTemplate.queryForObject("select emp_id,emp_name,designation,profile_pic from employee where emp_id=? and delete_status=false",(rs, rowNum) -> {
                return new EmployeeProfile(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),rs.getString("profile_pic"));
            },empId);
        }
        catch (DataAccessException e)
        {
            return null;
        }
    }
    //attended course
    public List<AttendedCourse> attendedCourse(String empId)
    {
        try
        {
            return jdbcTemplate.query("select Course.courseId,courseName,trainer,trainingMode,startDate,endDate from Course,AcceptedInvites where Course.courseId = AcceptedInvites.courseid and Course.completionStatus='completed' and AcceptedInvites.deleteStatus=false and AcceptedInvites.empId=?",(rs, rowNum) -> {
                return new AttendedCourse(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"));
            },empId);
        }
        catch (DataAccessException e)
        {
            return null;
        }
    }

    public List<NonAttendedCourse> nonAttendedCourse(String empId)
    {
        try
        {
            return jdbcTemplate.query("select Course.courseId,courseName,trainer,trainingMode,startDate,endDate,reason from Course,RejectedInvites where Course.courseId = RejectedInvites.courseid  and RejectedInvites.empId=?",(rs, rowNum) -> {
                return new NonAttendedCourse(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"),rs.getString("reason"));
            },empId);
        }
        catch (DataAccessException e)
        {
            return null;
        }
    }


    //Omkar

    //Filter Accepted invites by Completed status based on date
    public List<Course> filterCourse(FilterByDate filter, String empId){
        if(filter.getCompletionStatus().matches("active|upcoming")){
            return filterCoursesForEmployeeByActiveOrUpcomingStatus(filter,empId);
        }
        return filterCoursesForEmployeeByCompletedStatus(filter,empId);
    }
    //Filter for Active and Upcoming Courses from Accepted Invites by Employee by date and status
    public List<Course> filterCoursesForEmployeeByActiveOrUpcomingStatus(FilterByDate filter, String empId){
        String query = "SELECT course.courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course, AcceptedInvites WHERE course.courseId = AcceptedInvites.courseID AND empId = ? AND AcceptedInvites.deleteStatus = 0 AND Course.completionStatus = ? AND (startDate >= ? and startDate <= ? )";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<Course>(Course.class),empId,filter.getCompletionStatus(),filter.getDownDate(),filter.getTopDate());
    }

    //Filter for Completed Courses from Accepted Invites by Employee by date and status
    public List<Course> filterCoursesForEmployeeByCompletedStatus(FilterByDate filter, String empId){
        String query = "SELECT course.courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course, AcceptedInvites WHERE course.courseId = AcceptedInvites.courseID AND empId = ? AND AcceptedInvites.deleteStatus = 0 AND Course.completionStatus = ? AND (endDate >= ? and endDate <= ? )";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<Course>(Course.class),empId,filter.getCompletionStatus(),filter.getDownDate(),filter.getTopDate());
    }



    //Get Count of Courses that employee has accepted the request based on completion status
    public int getCourseStatusCountForEmployee(String empId, String completionStatus){
        String query = "select count(acceptedinvites.courseId) from acceptedinvites, course where acceptedinvites.courseId = course.courseId and course.completionStatus = ? and empId = ? ";
        return jdbcTemplate.queryForObject(query, Integer.class,completionStatus,empId);
    }

    //to retrieve invited courses based on completion status
    public List<Course> coursesForEmployeeByCompletedStatus(String empId,String status)
    {
        String query = "SELECT course.courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus FROM Course, AcceptedInvites WHERE course.courseId = AcceptedInvites.courseID AND empId = ? AND AcceptedInvites.deleteStatus = 0 AND Course.completionStatus = ?";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<Course>(Course.class),empId,status);
    }

   // ---------to get fresh notification count---------

    //DESCRIPTION

//    public Integer notificationCount(String empId)
//    {
//
//    }
}
