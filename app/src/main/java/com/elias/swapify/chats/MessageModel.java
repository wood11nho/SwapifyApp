package com.elias.swapify.chats;

import java.util.Date;

public class MessageModel {
    private String senderId;
    private String receiverId;
    private String content;
    private Date datetime;

    public MessageModel() {
        // Default constructor required for Firestore deserialization
    }

    public MessageModel(String senderId, String receiverId, String content, Date datetime) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.datetime = datetime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", content='" + content + '\'' +
                ", datetime=" + datetime +
                '}';
    }
}
