package com.e.dudusgram.models;

public class Chat {

    private String last_message;
    private String timestamp;
    private String room_id;

    public Chat(String last_message, String timestamp, String room_id) {
        this.last_message = last_message;
        this.timestamp = timestamp;
        this.room_id = room_id;
    }

    public Chat() {
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "last_message='" + last_message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", room_id='" + room_id + '\'' +
                '}';
    }
}
