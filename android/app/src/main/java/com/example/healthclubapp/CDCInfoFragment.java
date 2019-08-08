package com.example.healthclubapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class CDCInfoFragment extends Fragment {


    public CDCInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cdcinfo, container, false);

        // Seperate Sleep CDC Chart
        ImageView imgViewSleepCDC = (ImageView) view.findViewById(R.id.imageViewSleepCDC);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/sleep_cdc_benchmark2.jpg").fit().into(imgViewSleepCDC);

        // Seperate Calories Burned CDC Chart
        ImageView imgViewBurnedCDC = (ImageView) view.findViewById(R.id.imageViewBurnedCDC);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/calories_burned_cdc_benchmark2.jpg").fit().into(imgViewBurnedCDC);

        return view;
    }

}
