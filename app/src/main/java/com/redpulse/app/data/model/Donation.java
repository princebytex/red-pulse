package com.redpulse.app.data.model;

import com.google.firebase.Timestamp;

public class Donation {
    private String id;
    private String requestId;
    private String donorId;
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private String donorBloodGroup;
    private String receiverId;
    private String receiverName;
    private String requestBloodGroup;
    private String status;
    private boolean creditsGranted;
    private String note;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp confirmedAt;

    public Donation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public String getDonorEmail() { return donorEmail; }
    public void setDonorEmail(String donorEmail) { this.donorEmail = donorEmail; }
    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }
    public String getDonorBloodGroup() { return donorBloodGroup; }
    public void setDonorBloodGroup(String donorBloodGroup) { this.donorBloodGroup = donorBloodGroup; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getRequestBloodGroup() { return requestBloodGroup; }
    public void setRequestBloodGroup(String requestBloodGroup) { this.requestBloodGroup = requestBloodGroup; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isCreditsGranted() { return creditsGranted; }
    public void setCreditsGranted(boolean creditsGranted) { this.creditsGranted = creditsGranted; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public Timestamp getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }
}
