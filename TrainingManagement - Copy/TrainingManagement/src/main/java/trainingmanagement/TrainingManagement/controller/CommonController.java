package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.request.FilterByDate;
import trainingmanagement.TrainingManagement.response.CourseInfo;
import trainingmanagement.TrainingManagement.response.EmployeeDetails;
import trainingmanagement.TrainingManagement.response.EmployeeInvite;
import trainingmanagement.TrainingManagement.request.MultipleEmployeeRequest;
import trainingmanagement.TrainingManagement.response.EmployeeProfile;
import trainingmanagement.TrainingManagement.service.CommonService;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CommonController
{
    @Autowired
    CommonService commonService;
    @GetMapping("/attendees_nonAttendees_count/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<?> getAttendeesAndNonAttendeesCount(@PathVariable int courseId,Authentication authentication)
    {
        String employeeCount = commonService.getAttendeesAndNonAttendeesCount(courseId,authentication.getName());
        if (employeeCount == null)
        {
            return new ResponseEntity<>("This course is not allocated to you or there are no attendees for this course or the course may be deleted",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(employeeCount));
    }

    @GetMapping("/attendees/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<?> getAttendees(@PathVariable int courseId,Authentication authentication,@RequestParam int page, int limit)
    {
        Map<Integer,List<EmployeeProfile>> employee = commonService.getAttendingEmployee(courseId,authentication.getName(),page,limit);
        if (employee==null)
        {
            return new ResponseEntity<>("This course is not allocated to you or there are no attendees for this course",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(employee));
    }

    @GetMapping("/nonAttendees/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<?> getNonAttendees(@PathVariable int courseId,Authentication authentication,@RequestParam int page, int limit)
    {
        Map<Integer,List<EmployeeProfile>> employee = commonService.getNonAttendingEmployee(courseId,authentication.getName(),page,limit);
        if (employee == null)
        {
            return new ResponseEntity<>("This course is not allocated to you or there are no non-attendees for this course",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(employee));
    }

    //inviting employees
    @GetMapping("/getEmployeesToInvite/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<?> getEmployeesToInvite(@PathVariable int courseId,Authentication authentication)
    {
        List<EmployeeInvite> employeeList = commonService.getEmployeesToInvite(courseId,authentication.getName());
        if (employeeList.size() == 0)
        {
            return new ResponseEntity<>("There are no employees who are not invited or You cannot invite employees for this course",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(employeeList));
    }

    @PostMapping("/invite/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<String> inviteEmployee(@PathVariable int courseId, @RequestBody List<MultipleEmployeeRequest> inviteToEmployees,Authentication authentication)
    {
        String inviteStatus = commonService.inviteEmployees(courseId,inviteToEmployees,authentication.getName());
        if (inviteStatus == null)
        {
            return new ResponseEntity<>("You cannot invite employees for this course",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(inviteStatus));
    }

    @PutMapping("/deleteInvite/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<String> deleteInvite(@PathVariable int courseId,@RequestBody List<MultipleEmployeeRequest> deleteInvites,Authentication authentication)
    {
        String deleteStatus = commonService.deleteInvite(courseId,deleteInvites,authentication.getName());
        if (deleteStatus == null)
        {
            return new ResponseEntity<>("You cannot delete invite of this employees for this course",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(deleteStatus));
    }

    //omkar and sudarshan
    //Get List of All Employees
    @GetMapping("/employees")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<?> getEmployeeList(Authentication authentication)
    {
        String empId = authentication.getName();
        List<EmployeeDetails> empData = commonService.employeeDetails(empId);
        if (empData.size() == 0)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(empData));
    }


    @GetMapping("/employees/{searchKey}")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<List<EmployeeDetails>> getEmployeeListBySearchKey(Authentication authentication, @PathVariable String searchKey)
    {
        String empId = authentication.getName();

        List<EmployeeDetails> empData = commonService.employeeDetailsBySearchKey(empId,searchKey);
        if (empData.size() == 0)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(empData));
    }
    @GetMapping("/course/filter")
    @PreAuthorize("hasRole('admin') or hasRole('manager')")
    public ResponseEntity<List<Course>> filterCourse(Authentication authentication, @RequestBody FilterByDate filter){
        String empID = authentication.getName();

        List<Course> filteredCourseList = commonService.filteredCourses(empID,filter);
        if (filteredCourseList.size() == 0)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(filteredCourseList));
    }

}

