package com.redpulse.app.data.model;

import com.google.firebase.Timestamp;

public class AdminNotice {
    private String id;
    private String title;
    private String message;
    private String priority;
    private boolean active;
    private Timestamp createdAt;

    public AdminNotice() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
