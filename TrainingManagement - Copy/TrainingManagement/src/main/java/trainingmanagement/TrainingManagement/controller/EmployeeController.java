package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import trainingmanagement.TrainingManagement.response.AttendedNonAttendedCourse;
import trainingmanagement.TrainingManagement.response.EmployeeProfile;
import trainingmanagement.TrainingManagement.response.RejectedResponse;
import trainingmanagement.TrainingManagement.service.EmployeeService;

import java.util.List;
import java.util.Optional;

@RestController
public class EmployeeController
{
    @Autowired
    EmployeeService employeeService;

    @PutMapping("/acceptInvite/{inviteId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<?> acceptInvite(@PathVariable int inviteId)
    {
        String acceptanceStatus = employeeService.acceptInvite(inviteId);
        if (acceptanceStatus == null)
        {
            return new ResponseEntity<>("Invite not found", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(acceptanceStatus));
    }

    @PutMapping("/rejectInvite/{inviteId}")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<?> rejectInvite(@PathVariable int inviteId, @RequestBody RejectedResponse reason)
    {
        String rejectStatus = employeeService.rejectInvite(inviteId,reason);
        if (rejectStatus == null)
        {
            return new ResponseEntity<>("Invite not found", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(rejectStatus));
    }

    @GetMapping("/profileInfo")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<?> employeeProfile(Authentication authentication)
    {
        EmployeeProfile profileInfo = employeeService.profileInfo(authentication.getName());
        if (profileInfo == null)
        {
            return new ResponseEntity<>("Employee not found", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(profileInfo));
    }

    @GetMapping("/attendedCourse")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<?> attendedCourses(Authentication authentication)
    {
        List<AttendedNonAttendedCourse> attendedNonAttendedCourses = employeeService.attendedCourse(authentication.getName());
        if (attendedNonAttendedCourses == null)
        {
            return new ResponseEntity<>("You did not attend any course", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(attendedNonAttendedCourses));
    }

}
