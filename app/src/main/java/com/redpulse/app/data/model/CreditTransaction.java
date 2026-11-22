package com.redpulse.app.data.model;

import com.google.firebase.Timestamp;

public class CreditTransaction {
    private String id;
    private String userId;
    private String type;
    private int points;
    private int balanceAfter;
    private String sourceDonationId;
    private String sourceRequestId;
    private String monthKey;
    private String description;
    private String createdBy;
    private Timestamp createdAt;

    public CreditTransaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public int getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getSourceDonationId() { return sourceDonationId; }
    public void setSourceDonationId(String sourceDonationId) { this.sourceDonationId = sourceDonationId; }
    public String getSourceRequestId() { return sourceRequestId; }
    public void setSourceRequestId(String sourceRequestId) { this.sourceRequestId = sourceRequestId; }
    public String getMonthKey() { return monthKey; }
    public void setMonthKey(String monthKey) { this.monthKey = monthKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
