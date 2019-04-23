package com.example.finalproject;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(foreignKeys = @ForeignKey(entity = Session.class,
                                    parentColumns = "sessionID",
                                    childColumns =  "sessionID"))
public class Round {
    @NonNull
    @PrimaryKey
    private String roundID;
    private String status;
    private String sessionID;

    public Round() {    }

    public Round(@NonNull String roundID, String status, String sessionID) {
        this.roundID = roundID;
        this.status = status;
        this.sessionID = sessionID;
    }

    //-----------Getters--------------

    public String getSessionID() { return sessionID; }

    public String getRoundID() {
        return roundID;
    }

    public String getStatus() {
        return status;
    }

    //------------Setters----------

    public void setSessionID(String sessionID) { this.sessionID = sessionID; }

    public void setRoundID(@NonNull String roundID) {
        this.roundID = roundID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}


