package com.example.finalproject;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

@Entity(foreignKeys = @ForeignKey(entity = Round.class,
                                    parentColumns = "roundID",
                                    childColumns =  "roundID"))
public class Job {
    @NonNull
    @PrimaryKey
    private String jobID;
    private String jobType;
    private String name;
    private String postcode;
    private String address;
    private String phoneNumber;
    private String status;
    private String client;
    private String roundID;
    @Nullable private String latlng;
    @Nullable private int order;

    public Job() {}

    public Job(@NonNull String jobID, String jobType, String name, String postcode, String address, String phoneNumber, String status, String client, String roundID) {
        this.jobID = jobID;
        this.jobType = jobType;
        this.name = name;
        this.postcode = postcode;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.client = client;
        this.roundID = roundID;
    }

    //---------- Getters -------------------

    public String getRoundID() { return roundID; }

    public String getJobID() {
        return jobID;
    }

    public String getJobType() {
        return jobType;
    }

    public String getName() {
        return name;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getClient() {
        return client;
    }

    @Nullable
    public String getLatlng() {
        return latlng;
    }

    public int getOrder() {
        return order;
    }
    //------------- Setters ---------------

    public void setRoundID(String roundID) { this.roundID = roundID; }

    public void setJobID(@NonNull String jobID) {
        this.jobID = jobID;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setLatlng(@Nullable String latlng) {
        this.latlng = latlng;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

