package com.e.dudusgram.models;

public class Members {

    private String firstUser;
    private String secondUser;

    public Members() {
    }

    public Members(String firstUser, String secondUser) {
        this.firstUser = firstUser;
        this.secondUser = secondUser;
    }

    public String getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(String firstUser) {
        this.firstUser = firstUser;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(String secondUser) {
        this.secondUser = secondUser;
    }

    @Override
    public String toString() {
        return "Members{" +
                "firstUser='" + firstUser + '\'' +
                ", secondUser='" + secondUser + '\'' +
                '}';
    }
}
