package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import trainingmanagement.TrainingManagement.customException.CourseInfoIntegrityException;
import trainingmanagement.TrainingManagement.entity.Course;
import trainingmanagement.TrainingManagement.request.ManagerEmployees;
import trainingmanagement.TrainingManagement.request.MultipleEmployeeRequest;
import trainingmanagement.TrainingManagement.response.CourseList;
import trainingmanagement.TrainingManagement.response.EmployeeInfo;
import trainingmanagement.TrainingManagement.service.AdminService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController
{
    @Autowired
    AdminService adminRepository;

    @GetMapping("/company/activeTrainings/count")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> activeCourseCount()
    {
        int count = adminRepository.activeCourse();
        if (count == 0)
        {
            return new ResponseEntity<>("No active course in the company",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(count));
    }

    @GetMapping("/company/upcomingTrainings/count")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> upcomingCourseCount()
    {
        int count = adminRepository.upcomingCourse();
        if (count == 0)
        {
            return new ResponseEntity<>("No upcoming course in the company",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(count));
    }

    @GetMapping("/company/completedTrainings/count")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> completedCourseCount()
    {
        int count = adminRepository.completedCourse();
        if (count == 0)
        {
            return new ResponseEntity<>("No completed course in the company",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(count));
    }
    //get the list of course based on completion status
    @GetMapping("/company/courses/{completionStatus}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getCourse(@PathVariable String completionStatus)
    {
        List<CourseList> courses = adminRepository.getCourse(completionStatus);
        if (courses.size() == 0)
        {
            return new ResponseEntity<>("No "+completionStatus+" course in the company",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(courses));
    }
    //create course
    @PostMapping("/createCourse")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> createCourse(@RequestBody Course course)
    {
        String course1 = null;
        try {
            course1 = adminRepository.createCourse(course);
        } catch (CourseInfoIntegrityException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.OK);
        }
        if (course1 == null)
        {
            return new ResponseEntity<>("Course is not created,please fill all the mandatory fields",HttpStatus.NOT_MODIFIED);
        }
        return ResponseEntity.of(Optional.of(course1));
    }

    //to allocate course to manager
    //displays list of upcoming courses
    @GetMapping("/allCourse")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getCourseToAssignManager(@RequestParam int page, @RequestParam int limit)
    {
        Map<Integer,List<CourseList>> courseList = adminRepository.getCourseToAssignManager(page,limit);
        if (courseList == null)
        {
            return new ResponseEntity<>("No more upcoming course in the company, create a course to assign to manager",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(courseList));
    }
    //gets the list of managers
    @GetMapping("/managers")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getManagers(@RequestParam int page, @RequestParam int limit)
    {
        Map<Integer,List<EmployeeInfo>> managers = adminRepository.getManagers(page,limit);
        if (managers == null)
        {
            return new ResponseEntity<>("No more managers in the company",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(managers));
    }

    //search managers by search key
    @GetMapping("/managersBySearchKey/{searchKey}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getManagersBySearchKey(@RequestParam int page, @RequestParam int limit,@PathVariable String searchKey)
    {
        Map<Integer,List<EmployeeInfo>> managers = adminRepository.getManagersBySearchkey(page,limit,searchKey);
        if (managers == null)
        {
            return new ResponseEntity<>("No more managers in the company",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(managers));
    }

    //assigns course to managers
    @PostMapping("/assignCourseToManager/{courseId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> assignCourseToManager(@PathVariable int courseId, @RequestBody List<MultipleEmployeeRequest> courseToManager)
    {
        String assignStatus = adminRepository.assignCourseToManager(courseId,courseToManager);
        if ( assignStatus == null )
        {
            return new ResponseEntity<>("This course is already allocated to this manager",HttpStatus.NOT_MODIFIED);
        }
        return ResponseEntity.of(Optional.of(assignStatus));
    }

//    //Omkar
//    @GetMapping("/getCourseCount/{empId}")
//    @PreAuthorize("hasRole('admin')")
//    public ResponseEntity<EmployeeList> getEmployeeCourseCount(@PathVariable String empId){
//        EmployeeList employeeList = adminRepository.getCountOfCourses(empId);
//
//        if(employeeList == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.of(Optional.of(employeeList));
//    }
//
//
//    //Delete Attendees from Invites for an Upcoming Course
//    @PutMapping("/removeAttendees")
//    @PreAuthorize("hasRole('admin')")
//    public ResponseEntity<String> removeAttendees(@PathVariable String empId, @PathVariable String courseId){
//        String removed = adminRepository.deleteAttendeeFromUpcomingCourse(empId,courseId);
//
//        if (removed == null){
//            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
//        }
//        return ResponseEntity.of(Optional.of(removed));
//    }


//11-11-2022

    @PatchMapping("/update/course")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> updateCourse(@RequestBody Course course){
        int updatedCourse = 0;
        try {
            updatedCourse = adminRepository.updateCourse(course);
        } catch (CourseInfoIntegrityException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_MODIFIED);
        }
        if(updatedCourse == 0){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.of(Optional.of(updatedCourse));
    }

    @PostMapping("/assignManager/employees")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> assignManager(@RequestBody ManagerEmployees managerEmployees)
    {
        try
        {
            adminRepository.assignEmployeesToManager(managerEmployees);
            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @DeleteMapping("/delete/course/{courseId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> deleteEmployees(@PathVariable Integer courseId)
    {
        try {
            adminRepository.deleteCourse(courseId);
            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
