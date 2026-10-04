package com.example.enterprise_rag_chatbot.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PiiMaskingService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b\\d{10}\\b|\\b\\d{3}[-.\\s]\\d{3}[-.\\s]\\d{4}\\b");

    private static final Pattern POLICY_NUMBER_PATTERN =
            Pattern.compile("\\b[A-Z]{2,4}-?\\d{6,10}\\b");

    public String mask(String text) {
        if (text == null) return null;

        String masked = EMAIL_PATTERN.matcher(text).replaceAll("***EMAIL_MASKED***");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("***PHONE_MASKED***");
        masked = POLICY_NUMBER_PATTERN.matcher(masked).replaceAll("***POLICY_MASKED***");

        return masked;
    }
}