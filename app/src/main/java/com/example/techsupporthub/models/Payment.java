package com.example.techsupporthub.models;

public class Payment {
    String id, technicianId, userId, amount, phone;
    boolean paid;
    long timestamp;

    public Payment() {
    }

    public Payment(String id, String technicianId, String userId, String amount, String phone, boolean paid, long timestamp) {
        this.id = id;
        this.technicianId = technicianId;
        this.userId = userId;
        this.amount = amount;
        this.phone = phone;
        this.paid = paid;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
