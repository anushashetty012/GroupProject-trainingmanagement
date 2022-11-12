package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.response.AcceptedOrRejectedResponse;
import trainingmanagement.TrainingManagement.response.AttendedNonAttendedCourse;
import trainingmanagement.TrainingManagement.response.EmployeeProfile;
import trainingmanagement.TrainingManagement.response.RejectedResponse;

import java.util.List;

@Service
public class EmployeeService
{
    @Autowired
    JdbcTemplate jdbcTemplate;

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
    public List<AttendedNonAttendedCourse> attendedCourse(String empId)
    {
        try
        {
            return jdbcTemplate.query("select Course.courseId,courseName,trainer,trainingMode,startDate,endDate from Course,AcceptedInvites where Course.courseId = AcceptedInvites.courseid and Course.completionStatus='completed' and AcceptedInvites.deleteStatus=false and AcceptedInvites.empId=?",(rs, rowNum) -> {
                return new AttendedNonAttendedCourse(rs.getInt("courseId"),rs.getString("courseName"),rs.getString("trainer"),rs.getString("trainingMode"),rs.getDate("startDate"),rs.getDate("endDate"));
            },empId);
        }
        catch (DataAccessException e)
        {
            return null;
        }
    }

//    public List<AttendedNonAttendedCourse> nonAttendedCourse(String empId)
//    {
//        try
//        {
//            return jdbcTemplate.query()
//        }
//    }
}
