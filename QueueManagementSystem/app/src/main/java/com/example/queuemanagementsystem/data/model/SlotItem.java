package com.example.queuemanagementsystem.data.model;

public class SlotItem {

    private String slotId;
    private String serviceId;
    private String date;
    private String startTime;
    private String endTime;
    private long capacity;
    private long bookedCount;
    private boolean active;
    private long nextQueueNumber;

    public SlotItem() {
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getBookedCount() {
        return bookedCount;
    }

    public void setBookedCount(long bookedCount) {
        this.bookedCount = bookedCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getNextQueueNumber() {
        return nextQueueNumber;
    }

    public void setNextQueueNumber(long nextQueueNumber) {
        this.nextQueueNumber = nextQueueNumber;
    }
}