package com.redpulse.app.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class ValidationUtils {
    private static final Set<String> BLOOD_GROUPS = new HashSet<>(Arrays.asList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    ));

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.trim().length() >= 6;
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidPhone(String value) {
        if (value == null) return false;
        String normalized = value.replaceAll("[^0-9]", "");
        return normalized.length() >= 11 && normalized.length() <= 14;
    }

    public static String normalizeBloodGroup(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.getDefault()).replace(" ", "");
    }

    public static boolean isValidBloodGroup(String value) {
        return BLOOD_GROUPS.contains(normalizeBloodGroup(value));
    }
}
