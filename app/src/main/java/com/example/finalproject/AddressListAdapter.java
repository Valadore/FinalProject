package com.example.finalproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

public class AddressListAdapter extends BaseAdapter implements ListAdapter {
    private List<String> list;
    private Context context;

    AddressListAdapter(List<String> list, Context context) {
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
            Log.d("HELP", "getView: We made it this far");
            view = inflater.inflate(R.layout.address_list, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = view.findViewById(R.id.list_item_string);
        listItemText.setText((position + 1) + ": " + list.get(position));

        return view;
    }
}