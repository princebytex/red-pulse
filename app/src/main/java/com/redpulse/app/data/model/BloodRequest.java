package com.redpulse.app.data.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class BloodRequest {
    private String id;
    private String requesterId;
    private String requesterName;
    private String requesterPhone;
    private String patientName;
    private String bloodGroup;
    private int requiredUnits;
    private String hospitalName;
    private String fullAddress;
    private String contactNumber;
    private Timestamp requiredTimestamp;
    private boolean emergency;
    private String notes;
    private String status;
    private boolean active;
    private List<String> acceptedDonorIds;
    private int matchedDonorCount;
    private String collectorUserId;
    private String adminDecisionReason;
    private String cancellationReason;
    private String completionNote;
    private String reviewedBy;
    private Timestamp reviewedAt;
    private Timestamp publishedAt;
    private Timestamp closedAt;
    private Double latitude;
    private Double longitude;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public BloodRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getRequiredUnits() { return requiredUnits; }
    public void setRequiredUnits(int requiredUnits) { this.requiredUnits = requiredUnits; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Timestamp getRequiredTimestamp() { return requiredTimestamp; }
    public void setRequiredTimestamp(Timestamp requiredTimestamp) { this.requiredTimestamp = requiredTimestamp; }
    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<String> getAcceptedDonorIds() { return acceptedDonorIds; }
    public void setAcceptedDonorIds(List<String> acceptedDonorIds) { this.acceptedDonorIds = acceptedDonorIds; }
    public int getMatchedDonorCount() { return matchedDonorCount; }
    public void setMatchedDonorCount(int matchedDonorCount) { this.matchedDonorCount = matchedDonorCount; }
    public String getCollectorUserId() { return collectorUserId; }
    public void setCollectorUserId(String collectorUserId) { this.collectorUserId = collectorUserId; }
    public String getAdminDecisionReason() { return adminDecisionReason; }
    public void setAdminDecisionReason(String adminDecisionReason) { this.adminDecisionReason = adminDecisionReason; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getCompletionNote() { return completionNote; }
    public void setCompletionNote(String completionNote) { this.completionNote = completionNote; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
    public Timestamp getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Timestamp publishedAt) { this.publishedAt = publishedAt; }
    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
