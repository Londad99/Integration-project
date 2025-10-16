package com.example.Integration.project.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    private record VerificationData(String code, OffsetDateTime expiresAt) {}

    private final Map<String, VerificationData> codes = new ConcurrentHashMap<>();

    public String generateCode(String email) {
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        codes.put(email, new VerificationData(code, OffsetDateTime.now().plusMinutes(5)));
        return code;
    }

    public boolean verifyCode(String email, String code) {
        VerificationData data = codes.get(email);
        if (data == null) return false;
        if (OffsetDateTime.now().isAfter(data.expiresAt)) {
            codes.remove(email);
            return false;
        }
        boolean valid = data.code.equals(code);
        if (valid) codes.remove(email);
        return valid;
    }
}
