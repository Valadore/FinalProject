package com.example.finalproject.ui.barcodescan;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalproject.AppDatabase;
import com.example.finalproject.Parcel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.example.finalproject.R;

public class BarcodeScanFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private List<String> barcodeList;
    private List<String> scannedBarcodes;
    private Parcel[] parcelList;
    private AppDatabase db;


    public static BarcodeScanFragment newInstance() {
        return new BarcodeScanFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.barcode_scan_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //build database !!!!temp useing main thread need to change!!!!!!!!!!
         db = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        barcodeView = getView().findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getActivity().getIntent());
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(getActivity());
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String barcode = result.getText();
            //check for data, if the barcode has been scanned, and if the barcxode existsin our db
            if(barcode == null || scannedBarcodes.contains(barcode) || !barcodeList.contains(barcode)) {
                barcodeView.setStatusText("Not in list: " + barcode);
                // Prevent duplicate scans
                return;
            }

            scannedBarcodes.add(barcode);
            barcodeView.setStatusText(barcode);
            beepManager.playBeepSoundAndVibrate();

            //update parcel info
            Parcel parcel = db.myDao().getParcelByBarcode(barcode);
            parcel.setStatus("Scanned");
            db.myDao().updateParcel(parcel);

            //update info on screen
            TextView tvScanned = getView().findViewById(R.id.tv_scanned);
            tvScanned.setText("Scanned: " + scannedBarcodes.size() + "/" + barcodeList.size());
            TextView tvName = getView().findViewById(R.id.tv_Name);
            tvName.setText(db.myDao().getNameByBarcode(barcode));
            TextView tvAddress = getView().findViewById(R.id.tv_address);
            tvAddress.setText(db.myDao().getAddressByBarcode(barcode));

            //Added preview of scanned barcode
            ImageView imageView = getView().findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        Log.d("resueme?", "WE HAVE RESUMED ");

        barcodeList = db.myDao().getAllBarcodes();
        scannedBarcodes = new ArrayList<>();

        parcelList = db.myDao().getAllParcels();

        for (int i = 0; i < parcelList.length; i++)
        {
            Log.d("parcel status", "Status: " + parcelList[i].getStatus());
            if (parcelList[i].getStatus() == "Scanned")
            {
                scannedBarcodes.add(parcelList[i].getParcelBarcode());
                Log.d("Scanned Barcode:", parcelList[i].getParcelBarcode());
            }
        }

        Log.d("Parcel List",String.valueOf( parcelList.length));


        TextView tvScanned = getView().findViewById(R.id.tv_scanned);
        tvScanned.setText("Scanned: " + scannedBarcodes.size() + "/" + barcodeList.size());

        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

}
