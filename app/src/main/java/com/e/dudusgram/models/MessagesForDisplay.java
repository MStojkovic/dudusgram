package com.e.dudusgram.models;

public class MessagesForDisplay implements Comparable<MessagesForDisplay>{

    private String username;
    private String profile_picture;
    private String message;
    private String timestamp;

    public MessagesForDisplay() {
    }

    public MessagesForDisplay(String username, String profile_picture, String message, String timestamp) {
        this.username = username;
        this.profile_picture = profile_picture;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
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
        return "MessagesForDisplay{" +
                "username='" + username + '\'' +
                ", profile_picture='" + profile_picture + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }


    @Override
    public int compareTo(MessagesForDisplay o) {
        int last = this.timestamp.compareTo(o.timestamp);
        return last == 0 ? this.timestamp.compareTo(o.timestamp) : last;
    }
}
