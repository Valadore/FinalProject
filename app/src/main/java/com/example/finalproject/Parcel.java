package com.example.finalproject;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

@Entity(foreignKeys = @ForeignKey(entity = Job.class,
                                    parentColumns = "jobID",
                                    childColumns =  "jobID"))
public class Parcel {
    @NonNull
    @PrimaryKey
    private String parcelBarcode;
    private String status;
    private String jobID;
    @Nullable private String signatureFileName;
    @Nullable private String photoFileName;
    @Nullable private String GPS;
    @Nullable private Date time;

    public Parcel() {}

    public Parcel(@NonNull String parcelBarcode, String status, String jobID) {
        this.parcelBarcode = parcelBarcode;
        this.status = status;
        this.jobID = jobID;
    }

    //------Getters----------------

    public String getJobID() { return jobID; }

    public String getParcelBarcode() {
        return parcelBarcode;
    }

    public String getStatus() {
        return status;
    }

    @Nullable
    public String getSignatureFileName() {
        return signatureFileName;
    }

    @Nullable
    public String getPhotoFileName() {
        return photoFileName;
    }

    @Nullable
    public String getGPS() {
        return GPS;
    }

    @Nullable
    public Date getTime() {
        return time;
    }

    //-------- Setters -----------

    public void setJobID(String jobID) { this.jobID = jobID; }

    public void setParcelBarcode(@NonNull String parcelBarcode) {
        this.parcelBarcode = parcelBarcode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSignatureFileName(@Nullable String signatureFileName) {
        this.signatureFileName = signatureFileName;
    }

    public void setPhotoFileName(@Nullable String photoFileName) {
        this.photoFileName = photoFileName;
    }

    public void setGPS(@Nullable String GPS) {
        this.GPS = GPS;
    }

    public void setTime(@Nullable Date time) {
        this.time = time;
    }
}

