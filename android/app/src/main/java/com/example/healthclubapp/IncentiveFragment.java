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
public class IncentiveFragment extends Fragment {


    public IncentiveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_incentive, container, false);

        ImageView viewAwaken = (ImageView) view.findViewById(R.id.imageView_incentive_awaken);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/incentive_awaken2.jpg").fit().into(viewAwaken);

        ImageView viewBeingHuman = (ImageView) view.findViewById(R.id.imageView_incentive_being_human);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/incentive_being_human2.jpg").fit().into(viewBeingHuman);

        ImageView viewEatingTooMuch = (ImageView) view.findViewById(R.id.imageView_incentive_eat_too_much);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/incentive_eatingtoomuch.jpg").fit().into(viewEatingTooMuch);

        return view;
    }

}
