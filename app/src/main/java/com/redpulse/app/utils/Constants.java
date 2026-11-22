package com.redpulse.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Constants {
    public static final String USERS = "users";
    public static final String BLOOD_REQUESTS = "blood_requests";
    public static final String DONATIONS = "donations";
    public static final String CREDIT_TRANSACTIONS = "credit_transactions";
    public static final String NOTIFICATIONS = "notifications";
    public static final String ADMIN_NOTICES = "admin_notices";
    public static final String LEADERBOARDS = "leaderboards";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static final String STATUS_PENDING_REVIEW = "pending_review";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_CANCELLED_BY_REQUESTER = "cancelled_by_requester";
    public static final String STATUS_CONFIRMED_BY_RECEIVER = "confirmed_by_receiver";
    public static final String STATUS_INTEREST_SUBMITTED = "interest_submitted";

    public static final String CITY_CORPORATION_NONE = "Not under City Corporation";
    public static final String UNION_NOT_APPLICABLE = "Not applicable / No Union";
    public static final int DEFAULT_DONATION_CREDIT = 20;
    public static final int DEFAULT_REGISTRATION_CREDIT = 1;
    public static final int REQUEST_CREDIT_COST = 1;

    private Constants() {}

    public static String currentMonthKey() {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
    }

    public static String statusLabel(String status) {
        if (STATUS_PENDING_REVIEW.equals(status)) return "Pending admin review";
        if (STATUS_PUBLISHED.equals(status)) return "Approved & live";
        if (STATUS_REJECTED.equals(status)) return "Rejected";
        if (STATUS_CONFIRMED_BY_RECEIVER.equals(status)) return "Blood marked given";
        if (STATUS_COMPLETED.equals(status)) return "Completed";
        if (STATUS_CANCELLED.equals(status)) return "Closed by requester";
        if (STATUS_CANCELLED_BY_REQUESTER.equals(status)) return "Donor cancelled";
        if (STATUS_INTEREST_SUBMITTED.equals(status)) return "Interest submitted";
        if (status == null || status.trim().isEmpty()) return "Unknown";
        return status.replace('_', ' ');
    }
}
