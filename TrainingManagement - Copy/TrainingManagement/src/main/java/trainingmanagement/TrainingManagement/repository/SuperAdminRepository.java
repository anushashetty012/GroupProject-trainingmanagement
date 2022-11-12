package trainingmanagement.TrainingManagement.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Repository;
import trainingmanagement.TrainingManagement.entity.Employee;

@Repository
public class SuperAdminRepository
{
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private JavaMailSender mailSender;

//    @Autowired
//    private PasswordEncoder passwordEncoder;

    //private String SUPER_ADMIN_LOGIN = "SELECT * FROM Employee WHERE empId=? and password=?";
    private String REGISTER_EMPLOYEES = "INSERT INTO Employee(empId,empName,password,designation,email) values(?,?,?,?,?)";

    public String registerUser(Employee employee)
    {
        jdbcTemplate.update(REGISTER_EMPLOYEES,employee.getEmpId(),employee.getEmpName(),employee.getPassword(),employee.getDesignation(),employee.getEmail());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("spring.email.from@gmail.com");
        message.setTo(employee.getEmail());
        String emailText ="Employee id: " + employee.getEmpId()+"\nPassword: "+employee.getPassword();
        message.setText(emailText);
        message.setSubject("Login credentials for Training management website");
        mailSender.send(message);
        return "Registration successful";
    }

}
