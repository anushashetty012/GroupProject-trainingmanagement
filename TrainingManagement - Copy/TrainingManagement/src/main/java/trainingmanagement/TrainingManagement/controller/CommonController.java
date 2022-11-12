package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.entity.Employee;
import trainingmanagement.TrainingManagement.response.CourseInfo;
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
    //changes
    @GetMapping("count/acceptedInvites/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<?> getAcceptedCount(@PathVariable int courseId,Authentication authentication)
    {
        int count = commonService.getAcceptedCount(courseId,authentication.getName());
        if (count == 0)
        {
            return new ResponseEntity<>("There are no attendees to this course or this course is not allocated to you",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(count));
    }

    @GetMapping("/courseDetails/{courseId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<?> viewCourseDetails(@PathVariable int courseId,Authentication authentication)
    {
        CourseInfo courseData = commonService.viewCourseDetails(courseId,authentication.getName());
        if (courseData == null)
        {
            return new ResponseEntity<>("No such course is allocated to you",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(courseData));
    }

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
            return new ResponseEntity<>("This course is not allocated to you or there are no attendees for this course",HttpStatus.NOT_FOUND);
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
}

