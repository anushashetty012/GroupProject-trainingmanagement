package trainingmanagement.TrainingManagement.service;

import org.springframework.stereotype.Service;

@Service
public class Constants {
    public static final String FIREBASE_SDK_JSON ="C:\\Users\\Anusha J Shetty\\Downloads\\trainingmanagementapp.json";//copy the sdk-config file root address, if its in root ,filename is enough
    public static final String FIREBASE_BUCKET = "trainingmanagementapp-5c5fc.appspot.com";//enter your bucket name
    public static final String FIREBASE_PROJECT_ID ="trainingmanagementapp-5c5fc";//enter your project id
    public static final String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/trainingmanagementapp-5c5fc.appspot.com/o/%s?alt=media";
}
