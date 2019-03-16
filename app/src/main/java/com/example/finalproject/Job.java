package com.example.finalproject;
import com.example.finalproject.Parcel;

public class Job {
    private String jobID;
    private String jobType;
    private String name;
    private String postcode;
    private String address;
    private String phoneNumber;
    private String status;
    private String client;
    private Parcel[] parcels;

    public Job() {}

    public Job(String jobID, String jobType, String name, String postcode, String address, String phoneNumber, String status, String client, Parcel[] parcels) {
        this.jobID = jobID;
        this.jobType = jobType;
        this.name = name;
        this.postcode = postcode;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.parcels = parcels;
        this.client = client;
    }

    //---------- Getters -------------------

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

    public Parcel[] getParcels() {
        return parcels;
    }

    public String getClient() {
        return client;
    }

    //------------- Setters ---------------

    public void setJobID(String jobID) {
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

    public void setParcels(Parcel[] parcels) {
        this.parcels = parcels;
    }

    public void setClient(String client) {
        this.client = client;
    }
}

