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
import android.graphics.Color;
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
import java.util.Objects;

import com.example.finalproject.R;

public class BarcodeScanFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private List<String> barcodeList;
    private List<String> scannedBarcodes;
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
         db = Room.databaseBuilder(Objects.requireNonNull(getActivity()),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        barcodeView = Objects.requireNonNull(getView()).findViewById(R.id.barcode_scanner);
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
            if(barcode == null || !barcodeList.contains(barcode)) {
                barcodeView.setStatusText("Not in list: " + barcode);
                // Prevent duplicate scans
                return;
            }
            if( scannedBarcodes.contains(barcode)) {
                barcodeView.setStatusText("Already Scanned: " + barcode);
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
            TextView tvScanned = Objects.requireNonNull(getView()).findViewById(R.id.tv_scanned);
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

    //when we resume we want to update the scanned barcodes so we dont duplicate scan
    @Override
    public void onResume() {
        super.onResume();

        Log.d("resueme?", "WE HAVE RESUMED ");

        barcodeList = db.myDao().getAllBarcodes();
        scannedBarcodes = new ArrayList<>();

        Parcel[] parcelList = db.myDao().getAllParcels();

        for (Parcel parcel : parcelList) {
            Log.d("parcel status", "Status: " + parcel.getStatus());
            if (parcel.getStatus().equals("Scanned")) {
                Log.d("parcel status", "Status: " + parcel.getStatus());
                scannedBarcodes.add(parcel.getParcelBarcode());
                Log.d("Scanned Barcode:", parcel.getParcelBarcode());
            }
        }

        Log.d("Parcel List",String.valueOf( parcelList.length));

        TextView tvScanned = Objects.requireNonNull(getView()).findViewById(R.id.tv_scanned);
        tvScanned.setText("Scanned: " + scannedBarcodes.size() + "/" + barcodeList.size());

        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        barcodeView.pause();
    }
}
