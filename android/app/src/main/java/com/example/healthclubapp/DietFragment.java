package com.example.healthclubapp;


import android.content.Context;
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
public class DietFragment extends Fragment {


    public DietFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_diet, container, false);
        ImageView viewDiet1 = (ImageView) view.findViewById(R.id.imageView_diet_button_1);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/diet_button_1.jpg").fit().into(viewDiet1);

        ImageView viewDiet2 = (ImageView) view.findViewById(R.id.imageView_diet_button_2);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/diet_button_2.jpg").fit().into(viewDiet2);

        ImageView viewDiet3 = (ImageView) view.findViewById(R.id.imageView_diet_button_3);
        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/diet_button_3.jpg").fit().into(viewDiet3);

        // Inflate the layout for this fragment
        return view;
    }

}
