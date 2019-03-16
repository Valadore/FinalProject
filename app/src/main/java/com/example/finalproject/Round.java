package com.example.finalproject;
import com.example.finalproject.Job;

public class Round {
    private String roundID;
    private String status;
    private Job[] jobs;

    public Round() {    }

    public Round(String roundID, String status, Job[] jobs) {
        this.roundID = roundID;
        this.status = status;
        this.jobs = jobs;
    }

    //-----------Getters--------------

    public String getRoundID() {
        return roundID;
    }

    public String getStatus() {
        return status;
    }

    public Job[] getJobs() {
        return jobs;
    }

    //------------Setters----------

    public void setRoundID(String roundID) {
        this.roundID = roundID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setJobs(Job[] jobs) {
        this.jobs = jobs;
    }
}


