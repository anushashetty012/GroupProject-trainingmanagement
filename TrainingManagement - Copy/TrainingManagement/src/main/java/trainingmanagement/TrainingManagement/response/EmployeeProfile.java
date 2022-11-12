package trainingmanagement.TrainingManagement.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeProfile
{
    private String empId;
    private String empName;
    private String designation;
    private String profilePic;
}
