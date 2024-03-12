package com.example.swapify.chats;

import java.util.Date;

public class MessageModel {
    private String currentUserId;
    private String userId;
    private String content;
    private Date datetime;

    public MessageModel() {
        // Default constructor required for Firestore deserialization
    }

    public MessageModel(String currentUserId, String userId, String content, Date datetime) {
        this.currentUserId = currentUserId;
        this.userId = userId;
        this.content = content;
        this.datetime = datetime;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "currentUserId='" + currentUserId + '\'' +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                ", datetime=" + datetime +
                '}';
    }
}
