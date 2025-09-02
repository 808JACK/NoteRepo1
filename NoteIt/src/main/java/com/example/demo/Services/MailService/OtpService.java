package com.example.demo.Services.MailService;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, String> otpCache = new ConcurrentHashMap<>();  // email -> otp

    public String generateOtp(){
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }

    public void saveOTP(String email,String otp){
        otpCache.put(email,otp);
    }

    public boolean verifyOTP(String email,String otp){
        String cachedOtp = otpCache.get(email);
        if (cachedOtp != null && cachedOtp.equals(otp)){
            otpCache.remove(email);
            return true;
        }
        return false;
    }
}
