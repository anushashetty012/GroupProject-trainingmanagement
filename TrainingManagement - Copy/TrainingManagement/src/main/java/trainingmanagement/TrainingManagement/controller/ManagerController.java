package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.service.ManagerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/manager")
public class ManagerController
{
    //Omkar
    @Autowired
    private ManagerService managerService;

    //Get List of courses assigned to manager based on the completion status

    @GetMapping("/assignedCourses/{completionStatus}")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<List<Course>> getCourse(Authentication authentication, @PathVariable String completionStatus){
        String empId = authentication.getName();
        List<Course> courseList = managerService.getAssignedCoursesForManagerByStatus(empId,completionStatus);
        if(courseList.size() == 0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(courseList));
    }
}
