package com.example.finalproject;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class BarcodeListAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<String> list;
    private Context context;
    private AppDatabase db;

    BarcodeListAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.barcode_list, null);
        }
          db = Room.databaseBuilder(view.getContext(),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        String barcode = list.get(position);

        //Handle TextView and display string from your list
        TextView listItemText = view.findViewById(R.id.list_item_string);
        String status = db.myDao().getStatusByBarcode(barcode);
        if (status.equals("Missing"))
        {
            listItemText.setText(barcode + " Missing");
            listItemText.setBackgroundColor(Color.RED);
        }else if (status.equals("Scanned"))
        {
            listItemText.setText(barcode + " Scanned");
            listItemText.setBackgroundColor(Color.GREEN);
        }
        else
        {
            listItemText.setText(barcode);
            listItemText.setBackgroundColor(Color.WHITE);
        }

        final String address = db.myDao().getAddressByBarcode(barcode);
        final String postcode = db.myDao().getPostcodeByBarcode(barcode);

        listItemText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String barcode = list.get(position);
                new AlertDialog.Builder(v.getContext())
                        .setMessage("Barcode: " + barcode + "\nAddress: " + address +
                                "\nPostcode: " + postcode)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Missing", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                String barcode = list.get(position);
                                Parcel tempParcel = db.myDao().getParcelByBarcode(barcode);
                                tempParcel.setStatus("Missing");
                                db.myDao().updateParcel(tempParcel);
                                notifyDataSetChanged();
                            }
                        }).show();
            }
        });
        return view;
    }
}
