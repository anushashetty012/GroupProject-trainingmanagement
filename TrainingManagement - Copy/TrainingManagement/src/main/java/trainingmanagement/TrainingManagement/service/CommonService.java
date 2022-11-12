package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.entity.Employee;
import trainingmanagement.TrainingManagement.entity.Invites;
import trainingmanagement.TrainingManagement.entity.ManagersCourses;
import trainingmanagement.TrainingManagement.response.CourseInfo;
import trainingmanagement.TrainingManagement.response.EmployeeInvite;
import trainingmanagement.TrainingManagement.request.MultipleEmployeeRequest;
import trainingmanagement.TrainingManagement.response.EmployeeProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommonService
{
    private String GET_ACCEPTED_COUNT = "SELECT COUNT(empId) FROM AcceptedInvites WHERE courseId=? and deleteStatus=false";
    //for admin
    private String VIEW_COURSE_DETAILS = "SELECT courseId,courseName,trainer,trainingMode,startDate,endDate,duration,startTime,endTime,completionStatus,meetingInfo FROM Course WHERE courseId=? and deleteStatus=false";
    //for manager
    private String CHECK_COURSE_ALLOCATION = "SELECT courseId FROM ManagersCourses WHERE managerId=? AND courseId=?";
    private String CHECK_IF_INVITED = "SELECT courseId FROM Invites where empId=? AND courseId=? and (acceptanceStatus=true or acceptanceStatus is null)";
    private String GET_ATTENDEES_COUNT = "SELECT COUNT(empId) FROM Invites WHERE courseId=? AND Invites.inviteId NOT IN (SELECT RejectedInvites.inviteId FROM RejectedInvites)";
    private String GET_NON_ATTENDEES_COUNT = "SELECT COUNT(empId) FROM Invites WHERE courseId=? AND Invites.inviteId IN (SELECT RejectedInvites.inviteId FROM RejectedInvites)";
    private String GET_COMPLETION_STATUS = "SELECT completionStatus FROM Course WHERE courseId=? and deleteStatus=false";
    private String GET_ATTENDEES = "SELECT emp_Id,emp_Name,designation,profile_pic FROM Employee,Invites WHERE Employee.emp_id<>'RT001' and Employee.emp_id=Invites.empId AND Invites.courseId=? AND Invites.inviteId NOT IN (SELECT RejectedInvites.inviteId FROM RejectedInvites) and delete_status=false LIMIT ?,?";
    private String GET_NON_ATTENDEES = "SELECT emp_Id,emp_Name,designation,profile_pic FROM Employee,RejectedInvites WHERE Employee.emp_id<>'RT001' and Employee.emp_id=RejectedInvites.empId AND RejectedInvites.courseId=? and delete_status=false LIMIT ?,?";
    //to get role
    private String GET_ROLE = "SELECT role_name FROM employee_role WHERE emp_id=?";

    //for inviting
    //employees for admin
    private String GET_INVITED_EMPLOYEES = "SELECT emp_id,emp_name,designation FROM Employee,Invites WHERE Employee.emp_id<>'RT001' and Employee.emp_id=Invites.empId and courseId=? and (acceptanceStatus=true or acceptanceStatus is null) and delete_status=false";
    private String GET_NON_INVITED_EMPLOYEES = "SELECT emp_id,emp_name,designation FROM Employee WHERE Employee.emp_id<>'RT001' and Employee.emp_id NOT IN (SELECT Invites.empId FROM Invites WHERE courseId=? and (acceptanceStatus=true or acceptanceStatus is null)) and delete_status=false";
    //employees for manager
    private String GET_INVITED_EMPLOYEES_FOR_MANAGER = "SELECT emp_id,emp_name,designation FROM Employee,Manager,Invites WHERE Employee.emp_id=Manager.empId and Manager.managerId=? and Employee.emp_id<>'RT001' and Employee.emp_id=Invites.empId and courseId=? and (acceptanceStatus=true or acceptanceStatus is null) and delete_status=false";
    private String GET_NON_INVITED_EMPLOYEES_FOR_MANAGER = "SELECT emp_id,emp_name,designation FROM Employee,Manager WHERE Employee.emp_id=Manager.empId and Manager.managerId=? and Employee.emp_id<>'RT001' and Employee.emp_id NOT IN (SELECT Invites.empId FROM Invites WHERE courseId=? and (acceptanceStatus=true or acceptanceStatus is null)) and delete_status=false";
    private String INVITE_EMPLOYEES = "INSERT INTO Invites(empId,courseId) VALUES(?,?)";

    //to delete invie
    private String DELETE_INVITE_FROM_INVITES = "DELETE FROM Invites WHERE courseId=? AND empId=?";
    private String DELETE_FROM_ACCEPTED_INVITES = "Update AcceptedInvites SET deleteStatus=true WHERE courseId=? and empId=?";
    int offset=0;
    @Autowired
    JdbcTemplate jdbcTemplate;

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
    public String getAttendeesAndNonAttendeesCount(int courseId,String empId)
    {
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            try
            {
                int attendeesCount = jdbcTemplate.queryForObject(GET_ATTENDEES_COUNT,new Object[]{courseId},Integer.class);
                int nonAttendeesCount = jdbcTemplate.queryForObject(GET_NON_ATTENDEES_COUNT,new Object[]{courseId},Integer.class);
                String completionStatus = jdbcTemplate.queryForObject(GET_COMPLETION_STATUS,new Object[]{courseId},String.class);
                if (completionStatus.equalsIgnoreCase("active") || completionStatus.equalsIgnoreCase("upcoming"))
                {
                    return "Attendees: "+attendeesCount;
                }
                return "Attendees: "+attendeesCount+"\nNon Attendees: "+nonAttendeesCount;
            }
            catch (DataAccessException e)
            {
                return "No such courseId exist";
            }

        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            try
            {
                List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION, (rs, rowNum) -> {
                    return new ManagersCourses(rs.getInt("courseId"));
                }, empId, courseId);
                if (isCourseAssigned.size() != 0)
                {
                    int attendeesCount = jdbcTemplate.queryForObject(GET_ATTENDEES_COUNT,new Object[]{courseId},Integer.class);
                    int nonAttendeesCount = jdbcTemplate.queryForObject(GET_NON_ATTENDEES_COUNT,new Object[]{courseId},Integer.class);
                    String completionStatus = jdbcTemplate.queryForObject(GET_COMPLETION_STATUS,new Object[]{courseId},String.class);
                    if (completionStatus.equalsIgnoreCase("active") || completionStatus.equalsIgnoreCase("upcoming"))
                    {
                        return "Attendees: "+attendeesCount;
                    }
                    return "Attendees: "+attendeesCount+"\nNon Attendees: "+nonAttendeesCount;
                }
            }
            catch (DataAccessException e)
            {
                return "No such courseId exist";
            }
        }
        return null;
    }
    public Map<Integer,List<EmployeeProfile>> getAttendingEmployee(int courseId, String empId,int page,int limit)
    {
        Map map = new HashMap<Integer,List>();
        offset = limit *(page-1);
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            List<EmployeeProfile> employeeProfileList = jdbcTemplate.query(GET_ATTENDEES,(rs, rowNum) -> {
                return new EmployeeProfile(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),rs.getString("profile_pic"));
            },courseId,offset,limit);
            if (employeeProfileList.size()!=0)
            {
                map.put(employeeProfileList.size(),employeeProfileList);
                return map;
            }
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            try
            {
                List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION, (rs, rowNum) -> {
                    return new ManagersCourses(rs.getInt("courseId"));
                }, empId, courseId);
                if (isCourseAssigned.size() != 0 )
                {
                    List<EmployeeProfile> employeeProfileList = jdbcTemplate.query(GET_ATTENDEES,(rs, rowNum) -> {
                        return new EmployeeProfile(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),rs.getString("profile_pic"));
                    },courseId,offset,limit);
                    if (employeeProfileList.size()!=0)
                    {
                        map.put(employeeProfileList.size(),employeeProfileList);
                        return map;
                    }
                }
            }
            catch (DataAccessException e)
            {
                return null;
            }
        }
        return null;
    }
    public Map<Integer,List<EmployeeProfile>> getNonAttendingEmployee(int courseId,String empId,int page,int limit)
    {
        Map map = new HashMap<Integer,List>();
        offset = limit *(page-1);
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            List<EmployeeProfile> employeeProfileList = jdbcTemplate.query(GET_NON_ATTENDEES,(rs, rowNum) -> {
                return new EmployeeProfile(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),rs.getString("profile_pic"));
            },courseId,offset,limit);
            if (employeeProfileList.size()!=0)
            {
                map.put(employeeProfileList.size(),employeeProfileList);
                return map;
            }
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            try
            {
                List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION, (rs, rowNum) -> {
                    return new ManagersCourses(rs.getInt("courseId"));
                }, empId, courseId);
                if (isCourseAssigned.size() != 0 )
                {
                    List<EmployeeProfile> employeeProfileList = jdbcTemplate.query(GET_NON_ATTENDEES,(rs, rowNum) -> {
                        return new EmployeeProfile(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),rs.getString("profile_pic"));
                    },courseId,offset,limit);
                    if (employeeProfileList.size()!=0)
                    {
                        map.put(employeeProfileList.size(),employeeProfileList);
                        return map;
                    }
                }
            }
            catch (DataAccessException e)
            {
                return null;
            }
        }
        return null;
    }

    public List<EmployeeInvite> getEmployeesToInvite(int courseId,String empId)
    {
        List<EmployeeInvite> employeeList = new ArrayList<>();
        List<EmployeeInvite> employeeList1 = new ArrayList<>();
        List<EmployeeInvite> employeeList2= new ArrayList<>();
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            //Employees who are invited, accepted or not responded
            employeeList = jdbcTemplate.query(GET_INVITED_EMPLOYEES,(rs, rowNum) -> {
                return new EmployeeInvite(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),true);
            },courseId);
            //Employees who rejected the invites and employees who are not invited
            employeeList1 = jdbcTemplate.query(GET_NON_INVITED_EMPLOYEES,(rs, rowNum) -> {
                return new EmployeeInvite(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),false);
            },courseId);
            employeeList2.addAll(employeeList);
            employeeList2.addAll(employeeList1);
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            try
            {
                List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION, (rs, rowNum) -> {
                    return new ManagersCourses(rs.getInt("courseId"));
                }, empId, courseId);
                if (isCourseAssigned.size() != 0 )
                {
                    employeeList = jdbcTemplate.query(GET_INVITED_EMPLOYEES_FOR_MANAGER,(rs, rowNum) -> {
                        return new EmployeeInvite(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),true);
                    },empId,courseId);
                    employeeList1 = jdbcTemplate.query(GET_NON_INVITED_EMPLOYEES_FOR_MANAGER,(rs, rowNum) -> {
                        return new EmployeeInvite(rs.getString("emp_id"),rs.getString("emp_name"),rs.getString("designation"),false);
                    },empId,courseId);
                    employeeList2.addAll(employeeList);
                    employeeList2.addAll(employeeList1);
                }
            }
            catch (DataAccessException e)
            {
                return null;
            }
        }
        return employeeList2;
    }
    public String inviteEmployees(int courseId,List<MultipleEmployeeRequest> inviteToEmployees, String empId)
    {
        int noOfInvites = inviteToEmployees.size();
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            for (int i=0; i<noOfInvites; i++)
            {
                //already invited
                List<Employee> isInvited = jdbcTemplate.query("SELECT emp_id FROM Employee,Invites WHERE Employee.emp_id=? and Employee.emp_id<>'RT001' and Employee.emp_id=Invites.empId and courseId=? and (acceptanceStatus=true or acceptanceStatus is null)", (rs, rowNum) -> {
                    return new Employee(rs.getString("emp_Id"));
                }, inviteToEmployees.get(i).getEmpId(), courseId);
                if (isInvited.size() == 0) {
                    jdbcTemplate.update(INVITE_EMPLOYEES, new Object[]{inviteToEmployees.get(i).getEmpId(), courseId,});
                }
            }
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION, (rs, rowNum) -> {
                return new ManagersCourses(rs.getInt("courseId"));
            }, empId, courseId);
            if (isCourseAssigned.size() != 0)
            {
                for (int i=0; i<noOfInvites; i++)
                {
                    List<Employee> isInvited = jdbcTemplate.query("SELECT emp_id FROM Employee,Invites WHERE Employee.emp_id=? and Employee.emp_id<>'RT001' and Employee.emp_id=Invites.empId and courseId=? and (acceptanceStatus=true or acceptanceStatus is null)", (rs, rowNum) -> {
                        return new Employee(rs.getString("emp_Id"));
                    }, inviteToEmployees.get(i).getEmpId(), courseId);
                    if (isInvited.size() == 0)
                    {
                        jdbcTemplate.update(INVITE_EMPLOYEES,new Object[]{inviteToEmployees.get(i).getEmpId(),courseId});
                    }
                }
            }
            else
            {
                return "You cannot assign employees for this course";
            }
        }
        return "Invited successfully";
    }


    //to delete invite
    public String deleteInvite(int courseId, List<MultipleEmployeeRequest> deleteInvites,String empId)
    {
        int noOfInvites = deleteInvites.size();
        if(getRole((empId)).equalsIgnoreCase("admin"))
        {
            for(int i=0; i<noOfInvites; i++)
            {
                jdbcTemplate.update(DELETE_INVITE_FROM_INVITES, courseId, deleteInvites.get(i).getEmpId());
                jdbcTemplate.update(DELETE_FROM_ACCEPTED_INVITES, courseId, deleteInvites.get(i).getEmpId());
            }
        }
        if (getRole((empId)).equalsIgnoreCase("manager"))
        {
            List<ManagersCourses> isCourseAssigned = jdbcTemplate.query(CHECK_COURSE_ALLOCATION, (rs, rowNum) -> {
                return new ManagersCourses(rs.getInt("courseId"));
            }, empId, courseId);
            if (isCourseAssigned.size() != 0) {
                for (int i = 0; i < noOfInvites; i++) {
                    jdbcTemplate.update(DELETE_INVITE_FROM_INVITES, courseId, deleteInvites.get(i).getEmpId());
                    jdbcTemplate.update(DELETE_FROM_ACCEPTED_INVITES, courseId, deleteInvites.get(i).getEmpId());
                }
            }
            else
            {
                return "You cannot delete employees for this course";
            }
        }
        return "Deleted invite successfully";
    }
}
