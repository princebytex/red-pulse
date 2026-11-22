package com.redpulse.app.data.model;

public class HistoryItem {
    private String title;
    private String subtitle;
    private String badge;
    private long sortTime;

    public HistoryItem() {
    }

    public HistoryItem(String title, String subtitle, String badge, long sortTime) {
        this.title = title;
        this.subtitle = subtitle;
        this.badge = badge;
        this.sortTime = sortTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public long getSortTime() {
        return sortTime;
    }

    public void setSortTime(long sortTime) {
        this.sortTime = sortTime;
    }
}
