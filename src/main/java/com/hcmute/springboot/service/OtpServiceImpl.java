package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.OtpPurpose;
import com.hcmute.springboot.entity.OtpToken;
import com.hcmute.springboot.entity.User;
import com.hcmute.springboot.repository.OtpTokenRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional
public class OtpServiceImpl implements IOtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    @Override
    public String generateOtp(User user, OtpPurpose purpose) {
        if (user == null || purpose == null) {
            throw new IllegalArgumentException("User and purpose must not be null");
        }

        invalidateExistingOtp(user, purpose);

        int number = random.nextInt(900000) + 100000;
        String rawOtp = String.valueOf(number);

        String hashedOtp = BCrypt.hashpw(rawOtp, BCrypt.gensalt());
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000);

        OtpToken token = new OtpToken(user, purpose, hashedOtp, expiresAt);
        otpTokenRepository.save(token);

        return rawOtp;
    }

    @Override
    public boolean verifyOtp(User user, OtpPurpose purpose, String rawOtp) {
        if (user == null || purpose == null || rawOtp == null) {
            return false;
        }

        List<OtpToken> tokens = otpTokenRepository.findValidTokens(
                user, purpose, new Timestamp(System.currentTimeMillis()), PageRequest.of(0, 1)
        );
        if (tokens.isEmpty()) {
            return false;
        }

        OtpToken token = tokens.get(0);
        if (token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }
        if (token.getAttempts() >= 5) {
            return false;
        }
        if (token.isUsed()) {
            return false;
        }

        try {
            if (BCrypt.checkpw(rawOtp, token.getCodeHash())) {
                token.setUsed(true);
                otpTokenRepository.save(token);
                return true;
            } else {
                token.setAttempts(token.getAttempts() + 1);
                otpTokenRepository.save(token);
                return false;
            }
        } catch (Exception e) {
            token.setAttempts(token.getAttempts() + 1);
            otpTokenRepository.save(token);
            return false;
        }
    }

    @Override
    public void invalidateExistingOtp(User user, OtpPurpose purpose) {
        if (user != null && purpose != null) {
            otpTokenRepository.invalidateExistingByUserAndPurpose(user, purpose);
        }
    }
}
