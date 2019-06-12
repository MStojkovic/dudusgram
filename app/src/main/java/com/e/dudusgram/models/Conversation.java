package com.e.dudusgram.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Conversation implements Parcelable {

    private String profile_image;
    private String username;
    private String message;
    private String timestamp;
    private String conversation_id;

    public Conversation() {
    }

    public Conversation(String profile_image, String username, String message, String timestamp, String conversation_id) {
        this.profile_image = profile_image;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.conversation_id = conversation_id;
    }


    protected Conversation(Parcel in) {
        profile_image = in.readString();
        username = in.readString();
        message = in.readString();
        timestamp = in.readString();
        conversation_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(profile_image);
        dest.writeString(username);
        dest.writeString(message);
        dest.writeString(conversation_id);
        dest.writeString(timestamp);
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "profile_image='" + profile_image + '\'' +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", conversation_id='" + conversation_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
