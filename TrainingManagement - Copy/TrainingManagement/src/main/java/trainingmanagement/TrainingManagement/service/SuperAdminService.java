package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.customException.EmployeeNotExistException;
import trainingmanagement.TrainingManagement.dao.EmployeeDao;
import trainingmanagement.TrainingManagement.dao.RoleDao;
import trainingmanagement.TrainingManagement.entity.Employee;
import trainingmanagement.TrainingManagement.entity.EmployeeRole;
import trainingmanagement.TrainingManagement.entity.Roles;
import trainingmanagement.TrainingManagement.request.MultipleEmployeeRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SuperAdminService {

    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private AdminService adminService;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private String CHANGING_ROLES = "UPDATE employee_role SET role_name=? WHERE emp_id=?";


    public Employee registerNewEmployee(Employee employee){
        Roles roles = roleDao.findById("employee").get();
        Set<Roles> employeeRoles = new HashSet<>();

        employeeRoles.add(roles);
        employee.setRoles(employeeRoles);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("spring.email.from@gmail.com");
        message.setTo(employee.getEmail());
        String emailText ="Employee id: " + employee.getEmpId()+"\nPassword: "+employee.getPassword();
        message.setText(emailText);
        message.setSubject("Login credentials for Training management website");
        mailSender.send(message);
        if(employee.getEmpId().equalsIgnoreCase("RT001"))
        {
            return null;
        }
        employee.setPassword(getEncodedPassword(employee.getPassword()));
        String query =  "insert into manager(empId) values(?)";
        employeeDao.save(employee);
        jdbcTemplate.update(query,employee.getEmpId());
        return employee;
    }

    public String changeRole(EmployeeRole employeeRole)
    {
        jdbcTemplate.update(CHANGING_ROLES,employeeRole.getRoleName(),employeeRole.getEmpId());
        return "Role changed to "+employeeRole.getRoleName();
    }


    public String getEncodedPassword(String password)
    {
        return passwordEncoder.encode(password);
    }
    public void deleteEmployees(List<MultipleEmployeeRequest> empId) throws EmployeeNotExistException {
        for (MultipleEmployeeRequest emp:empId) {
            adminService.checkEmployeeExist(emp.getEmpId());
            deleteEmployee(emp.getEmpId());
        }
    }
    public void deleteEmployee(String empId)
    {
        String query="update employee set delete_status=1 where emp_id=?";
        jdbcTemplate.update(query,empId);
    }
}
