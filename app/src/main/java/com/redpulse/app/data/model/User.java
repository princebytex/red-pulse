package com.redpulse.app.data.model;

import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String fullName;
    private int age;
    private String bloodGroup;
    private String address;
    private String addressLine;
    private String division;
    private String district;
    private String zila;
    private String cityCorporation;
    private String upazila;
    private String unionName;
    private String email;
    private String mobileNumber;
    private String role;
    private int totalCredits;
    private boolean available;
    private boolean verified;
    private boolean phoneVerified;
    private String profileImageUrl;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Timestamp lastDonationAt;
    private boolean blocked;
    private boolean deleted;
    private String deletedReason;
    private String searchText;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getZila() { return zila; }
    public void setZila(String zila) { this.zila = zila; }
    public String getCityCorporation() { return cityCorporation; }
    public void setCityCorporation(String cityCorporation) { this.cityCorporation = cityCorporation; }
    public String getUpazila() { return upazila; }
    public void setUpazila(String upazila) { this.upazila = upazila; }
    public String getUnionName() { return unionName; }
    public void setUnionName(String unionName) { this.unionName = unionName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Timestamp getLastDonationAt() { return lastDonationAt; }
    public void setLastDonationAt(Timestamp lastDonationAt) { this.lastDonationAt = lastDonationAt; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public String getDeletedReason() { return deletedReason; }
    public void setDeletedReason(String deletedReason) { this.deletedReason = deletedReason; }
    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getSafeDisplayAddress() {
        return address != null ? address : "";
    }
}
