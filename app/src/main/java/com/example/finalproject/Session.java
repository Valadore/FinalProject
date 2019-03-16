package com.example.finalproject;

import java.util.Date;

public class Session {
    private String userID;
    private Date date;
    private String status;
    private Round[] rounds;

    public Session() {    }

    public Session(String userID, Date date, String status, Round[] rounds) {
        this.userID = userID;
        this.date = date;
        this.status = status;
        this.rounds = rounds;
    }

    //---------Getters-----------

    public String getUserID() {
        return userID;
    }

    public Date getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public Round[] getRounds() {
        return rounds;
    }

    //--------Setters------------

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRounds(Round[] rounds) {
        this.rounds = rounds;
    }
}
