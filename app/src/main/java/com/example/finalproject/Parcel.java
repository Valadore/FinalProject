package com.example.finalproject;

import android.media.Image;
import android.support.annotation.Nullable;

import java.sql.Time;
import java.util.Date;

public class Parcel {
    private String parcelBarcode;
    private String status;
    @Nullable private Image signiture;
    @Nullable private Image photo;
    @Nullable private String GPS;
    @Nullable private Date time;

    public Parcel() {}

    //only the barcode and status are set on creation
    public Parcel(String parcelBarcode, String status)
    {
        this.parcelBarcode = parcelBarcode;
        this.status = status;
    }

    //------Getters----------------


    public String getParcelBarcode() {
        return parcelBarcode;
    }

    public String getStatus() {
        return status;
    }

    @Nullable
    public Image getSigniture() {
        return signiture;
    }

    @Nullable
    public Image getPhoto() {
        return photo;
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


    public void setParcelBarcode(String parcelBarcode) {
        this.parcelBarcode = parcelBarcode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSigniture(@Nullable Image signiture) {
        this.signiture = signiture;
    }

    public void setPhoto(@Nullable Image photo) {
        this.photo = photo;
    }

    public void setGPS(@Nullable String GPS) {
        this.GPS = GPS;
    }

    public void setTime(@Nullable Date time) {
        this.time = time;
    }
}

