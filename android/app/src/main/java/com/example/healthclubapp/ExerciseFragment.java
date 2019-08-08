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
public class ExerciseFragment extends Fragment {


    public ExerciseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        ImageView viewDiet1 = (ImageView) view.findViewById(R.id.imageView_exercise_chart);

        Picasso.get().load("https://health-club-demo.firebaseapp.com/images/exercise_chart.jpg").fit().into(viewDiet1);

        return view;
    }

}
