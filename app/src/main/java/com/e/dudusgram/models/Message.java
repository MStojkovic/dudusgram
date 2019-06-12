package com.e.dudusgram.models;

public class Message {

    private String userID;
    private String message;
    private String timestamp;

    public Message() {
    }

    public Message(String userID, String message, String timestamp) {
        this.userID = userID;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "userID='" + userID + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
