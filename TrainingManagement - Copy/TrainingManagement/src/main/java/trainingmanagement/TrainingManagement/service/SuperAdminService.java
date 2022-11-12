package trainingmanagement.TrainingManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trainingmanagement.TrainingManagement.dao.EmployeeDao;
import trainingmanagement.TrainingManagement.dao.RoleDao;
import trainingmanagement.TrainingManagement.entity.Employee;
import trainingmanagement.TrainingManagement.entity.EmployeeRole;
import trainingmanagement.TrainingManagement.entity.Roles;

import java.util.HashSet;
import java.util.Set;

@Service
public class SuperAdminService {

    @Autowired
    private EmployeeDao employeeDao;

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

        employee.setPassword(getEncodedPassword(employee.getPassword()));

        return employeeDao.save(employee);
    }

    public String changeRole(EmployeeRole employeeRole)
    {
        jdbcTemplate.update(CHANGING_ROLES,employeeRole.getRoleName(),employeeRole.getEmpId());
        return "Role changed to "+employeeRole.getRoleName();
    }


    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
