package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import trainingmanagement.TrainingManagement.entity.Employee;
import trainingmanagement.TrainingManagement.entity.EmployeeRole;
import trainingmanagement.TrainingManagement.request.MultipleEmployeeRequest;
import trainingmanagement.TrainingManagement.service.SuperAdminService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/superAdmin")
public class SuperAdminController
{
    @Autowired
    SuperAdminService superAdminService;

    @PostMapping("/registerEmployees")
    @PreAuthorize("hasRole('super_admin')")
    public ResponseEntity<String> registerEmployees(@RequestBody Employee employee)
    {
        Employee registerStatus = superAdminService.registerNewEmployee(employee);
        if (registerStatus == null)
        {
            return new ResponseEntity<>("Employee already exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of( " Registration successful"));
    }

    @PutMapping("/changeRole")
    @PreAuthorize("hasRole('super_admin')")
    public ResponseEntity<String> changingRole(@RequestBody EmployeeRole employeeRole)
    {
        String roleStatus = superAdminService.changeRole(employeeRole);
        if (roleStatus == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.of(Optional.of( roleStatus));
    }

    @PutMapping("/delete/Employees")
    @PreAuthorize("hasRole('super_admin')")
    public ResponseEntity<String> deleteEmployees(@RequestBody List<MultipleEmployeeRequest> employees)
    {
        try
        {
            superAdminService.deleteEmployees(employees);
            return ResponseEntity.status(HttpStatus.OK).body("");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(e.getMessage());
        }
    }
}
