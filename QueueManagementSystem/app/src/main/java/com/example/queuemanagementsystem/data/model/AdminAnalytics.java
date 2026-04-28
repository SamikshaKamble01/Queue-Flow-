package com.example.queuemanagementsystem.data.model;

public class AdminAnalytics {

    private long totalTokens;
    private long waitingTokens;
    private long completedTokens;
    private long cancelledTokens;
    private long calledTokens;
    private long skippedTokens;
    private long averageWaitMinutes;

    public long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }

    public long getWaitingTokens() {
        return waitingTokens;
    }

    public void setWaitingTokens(long waitingTokens) {
        this.waitingTokens = waitingTokens;
    }

    public long getCompletedTokens() {
        return completedTokens;
    }

    public void setCompletedTokens(long completedTokens) {
        this.completedTokens = completedTokens;
    }

    public long getCancelledTokens() {
        return cancelledTokens;
    }

    public void setCancelledTokens(long cancelledTokens) {
        this.cancelledTokens = cancelledTokens;
    }

    public long getCalledTokens() {
        return calledTokens;
    }

    public void setCalledTokens(long calledTokens) {
        this.calledTokens = calledTokens;
    }

    public long getSkippedTokens() {
        return skippedTokens;
    }

    public void setSkippedTokens(long skippedTokens) {
        this.skippedTokens = skippedTokens;
    }

    public long getAverageWaitMinutes() {
        return averageWaitMinutes;
    }

    public void setAverageWaitMinutes(long averageWaitMinutes) {
        this.averageWaitMinutes = averageWaitMinutes;
    }
}