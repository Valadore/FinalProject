package com.example.finalproject;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity
public class Session{
    @NonNull
    @PrimaryKey
    private String sessionID;
    private String userID;
    private Date date;
    private String status;

    public Session() {    }

    public Session(@NonNull String sessionID, String userID, Date date, String status) {
        this.sessionID = sessionID;
        this.userID = userID;
        this.date = date;
        this.status = status;
    }

    //---------Getters-----------

    @NonNull
    public String getSessionID() { return sessionID; }

    public String getUserID() {
        return userID;
    }

    public Date getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    //--------Setters------------

    public void setSessionID(@NonNull String sessionID) { this.sessionID = sessionID; }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
