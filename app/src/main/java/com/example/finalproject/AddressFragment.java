package com.example.finalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AddressFragment extends Fragment {

    private static List<String> optimisedAddresses = new ArrayList<>();

    public static AddressFragment newInstance(List<String> list) {
        optimisedAddresses = list;
        return new AddressFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.address_fragment, container, false);

        ListView lv = (ListView)rootView.findViewById(R.id.address_list);
        lv.setAdapter(new AddressListAdapter(optimisedAddresses, getActivity()));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button button = getActivity().findViewById(R.id.button_continue);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.d("RETURN", "onClick: I have been clicked!");
            }
        });
    }
}
