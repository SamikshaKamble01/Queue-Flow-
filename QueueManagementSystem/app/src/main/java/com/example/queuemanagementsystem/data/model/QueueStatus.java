package com.example.queuemanagementsystem.data.model;

public class QueueStatus {

    private String serviceId;
    private long currentQueueNumber;
    private String currentTokenNumber;
    private boolean paused;
    private long lastUpdated;

    public QueueStatus() {
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public long getCurrentQueueNumber() {
        return currentQueueNumber;
    }

    public void setCurrentQueueNumber(long currentQueueNumber) {
        this.currentQueueNumber = currentQueueNumber;
    }

    public String getCurrentTokenNumber() {
        return currentTokenNumber;
    }

    public void setCurrentTokenNumber(String currentTokenNumber) {
        this.currentTokenNumber = currentTokenNumber;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}