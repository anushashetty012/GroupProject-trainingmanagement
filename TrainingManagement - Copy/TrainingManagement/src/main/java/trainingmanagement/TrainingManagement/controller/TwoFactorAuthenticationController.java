package trainingmanagement.TrainingManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import trainingmanagement.TrainingManagement.request.OtpRequest;
import trainingmanagement.TrainingManagement.response.OtpResponse;
import trainingmanagement.TrainingManagement.response.PasswordResponse;
import trainingmanagement.TrainingManagement.service.EmailOtpService;

import java.util.Optional;
import java.util.Random;

@RestController
public class TwoFactorAuthenticationController {
    @Autowired
    private EmailOtpService emailService;



    //This will run when employee will hit forget password button
    //Send OTP through mail to change password
    @RequestMapping(value = "/employee/otpMail", method = RequestMethod.PUT)
    public ResponseEntity<Object> send2faCodeinEmail(@RequestBody OtpRequest otpRequest) throws Exception {
        String twoFaCode = String.valueOf(new Random().nextInt(9999)+1000);

        emailService.sendEmail(otpRequest.getEmailId(),twoFaCode);
        emailService.update2FAProperties(otpRequest,twoFaCode);

        return new ResponseEntity<>(HttpStatus.OK);
    }



    //This is Entering OTP by employee

    @RequestMapping(value="/employee/checkCode", method=RequestMethod.PUT)
    public ResponseEntity<Object> verify(Authentication authentication, @RequestBody OtpResponse otpResponse) {
        String empID = authentication.getName();
        boolean isValid = emailService.checkCode(otpResponse,empID);

        if(isValid){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    //13-Nov-2022
    //Change Password
    @PutMapping("/employee/changePassword")
    @PreAuthorize("hasRole('admin') or hasRole('manager') or hasRole('employee')")
    public ResponseEntity<Integer> changePassword(Authentication authentication, @RequestBody PasswordResponse passwordResponse){
        String empId = authentication.getName();
        int change = emailService.changePassword(passwordResponse,empId);
        if(change == 0){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.of(Optional.of(change));

    }

}
