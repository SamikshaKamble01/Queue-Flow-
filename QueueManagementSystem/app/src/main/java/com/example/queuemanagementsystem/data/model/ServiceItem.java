package com.example.queuemanagementsystem.data.model;

public class ServiceItem {

    private String serviceId;
    private String name;
    private String prefix;
    private long slotCapacity;
    private long avgServiceMinutes;
    private boolean active;

    public ServiceItem() {
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getSlotCapacity() {
        return slotCapacity;
    }

    public void setSlotCapacity(long slotCapacity) {
        this.slotCapacity = slotCapacity;
    }

    public long getAvgServiceMinutes() {
        return avgServiceMinutes;
    }

    public void setAvgServiceMinutes(long avgServiceMinutes) {
        this.avgServiceMinutes = avgServiceMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}