package com.example.finalproject;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MyDao {
    //--------- Create entities -----------------------
    @Insert
    public void createSession(Session... newSession);

    @Insert
    public void createRound(Round... newRound);

    @Insert
    public void createJob(Job... newJob);

    @Insert
    public void createParcel(Parcel... newParcel);

    //---------- Get --------------------------------

    @Query("SELECT * FROM Round")
    public Round[] getAllRounds();

    @Query("SELECT * FROM Round WHERE roundID = :roundID")
    public Round getRoundByID(String roundID);

    @Query("SELECT * FROM Job")
    public Job[] getAllJobs();

    @Query("SELECT * FROM Job WHERE roundID = :roundID")
    public Job[] getJobsFromRound(String roundID);

    @Query("SELECT * FROM Job WHERE jobID = :jobID")
    public Job getJobByID(String jobID);

    @Query("SELECT * FROM Parcel")
    public Parcel[] getAllParcels();

    @Query("SELECT * FROM Parcel" +
            " INNER JOIN job ON Job.jobID = Parcel.jobID" +
            " WHERE Job.roundID = :roundID")
    public Parcel[] getParcelsFromRound(String roundID);

    @Query("SELECT * FROM Parcel WHERE jobID = :jobID")
    public Parcel[] getParcelsFromJob(String jobID);

    @Query("SELECT * FROM Parcel WHERE parcelBarcode = :barcode")
    public Parcel getParcelByBarcode(String barcode);

    @Query("SELECT DISTINCT address FROM Job")
    public List<String> getAllAddress();

    @Query("SELECT DISTINCT postcode FROM Job")
    public List<String> getAllPostcodes();

    @Query("SELECT parcelBarcode FROM Parcel")
    public List<String> getAllBarcodes();

    @Query("SELECT name FROM Job INNER JOIN Parcel ON Parcel.jobID = Job.jobID WHERE Parcel.parcelBarcode = :barcode")
    public String getNameByBarcode(String barcode);

    @Query("SELECT address FROM Job INNER JOIN Parcel ON Parcel.jobID = Job.jobID WHERE Parcel.parcelBarcode = :barcode")
    public String getAddressByBarcode(String barcode);

    @Query("SELECT postcode FROM Job INNER JOIN Parcel ON Parcel.jobID = Job.jobID WHERE Parcel.parcelBarcode = :barcode")
    public String getPostcodeByBarcode(String barcode);

    @Query("SELECT status FROM Parcel WHERE parcelBarcode = :barcode")
    public String getStatusByBarcode(String barcode);

    //---------- update ------------------

    @Update
    public void updateParcel(Parcel... newParcel);

    @Update
    public void updateJob(Job... newJob);

    @Update
    public void updateRound(Round... newRound);

    @Update
    public void updateSession(Session... newSession);

}
