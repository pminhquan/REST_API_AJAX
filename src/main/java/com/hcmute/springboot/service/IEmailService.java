package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.OtpPurpose;

public interface IEmailService {

    boolean sendOtpEmail(String to, String otp, OtpPurpose purpose);
}
