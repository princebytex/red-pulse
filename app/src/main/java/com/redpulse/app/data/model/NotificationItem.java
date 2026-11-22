package com.redpulse.app.data.model;

import com.google.firebase.Timestamp;

public class NotificationItem {
    private String id;
    private String targetUserId;
    private String title;
    private String body;
    private String type;
    private String referenceId;
    private boolean read;
    private String priority;
    private Timestamp createdAt;

    public NotificationItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
