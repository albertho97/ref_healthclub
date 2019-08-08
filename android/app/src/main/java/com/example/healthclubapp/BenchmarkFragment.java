package com.example.healthclubapp;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class BenchmarkFragment extends Fragment {

    private TableLayout tableLayout;
    List<Benchmark> products = new ArrayList<Benchmark>();

    public BenchmarkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_benchmark, container, false);
        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout_benchmark);
        loadDataFromFirestore();

        return view;
    }


    private void loadDataFromFirestore()
    {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Log.w("uid", user.getUid());
        firestore.collection("std_member").document(user.getUid()).collection("std_member_daily_benchmark")
                .orderBy("aggregated_date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Document", document.getId() + " => " + document.getData());
                                String aggregated_date = "";
                                String hours = "";
                                String calories_burned = "";
                                String cdc_sleep_status = "";
                                String cdc_calories_burned_status = "";

                                if (document.get("aggregated_date") != null) { aggregated_date = document.get("aggregated_date").toString(); }
                                if (document.get("hours_slept") != null) { hours = document.get("hours_slept").toString(); }
                                if (document.get("calories_burned") != null) { calories_burned = document.get("calories_burned").toString(); }
                                if (document.get("cdc_sleep_status") != null) { cdc_sleep_status = document.get("cdc_sleep_status").toString(); }
                                if (document.get("cdc_calories_burned_status") != null) { cdc_calories_burned_status = document.get("cdc_calories_burned_status").toString(); }

                                products.add(new Benchmark( aggregated_date, hours, calories_burned, cdc_sleep_status, cdc_calories_burned_status));
                            }
                            loadData();
                        } else {
                            Log.w("Document", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void loadData() {
        createColumns();
        fillData(products);
    }

    private void createColumns() {
        TableRow tableRow = new TableRow(getContext());
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Aggregated Date
        TextView textViewDate = new TextView(getContext());
        textViewDate.setText("Date");
        textViewDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewDate.setPadding(5, 5, 10, 0);
        tableRow.addView(textViewDate );

        // Hours Slept
        TextView textViewHours = new TextView(getContext());
        textViewHours.setText("Sleep\n(Hrs)");
        textViewHours.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewHours.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewHours);

        // Sleep CDC
        TextView textViewSleepCDC = new TextView(getContext());
        textViewSleepCDC.setText("Sleep Status\n(vs CDC)");
        textViewSleepCDC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewSleepCDC.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewSleepCDC);

        // Calories Burned
        TextView textViewBurned = new TextView(getContext());
        textViewBurned.setText("Burned\n(kCal)");
        textViewBurned.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewBurned.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewBurned);

        // Burned CDC
        TextView textViewBurnedCDC = new TextView(getContext());
        textViewBurnedCDC.setText("Burned\n(vs CDC)");
        textViewBurnedCDC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewBurnedCDC.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewBurnedCDC);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Add Divider
        tableRow = new TableRow(getContext());
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Aggregated Date
        textViewDate  = new TextView(getContext());
        textViewDate.setText("-----------------");
        textViewDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewDate.setPadding(5, 5, 10, 0);
        tableRow.addView(textViewDate);

        // Hours Slept
        textViewHours = new TextView(getContext());
        textViewHours.setText("---------");
        textViewHours.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewHours.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewHours);

        // Sleep CDC
        textViewSleepCDC = new TextView(getContext());
        textViewSleepCDC.setText("---------------------");
        textViewSleepCDC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewSleepCDC.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewSleepCDC);

        // Burned
        textViewBurned = new TextView(getContext());
        textViewBurned.setText("---------");
        textViewBurned.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewBurned.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewBurned);


        // Burned CDC
        textViewBurnedCDC = new TextView(getContext());
        textViewBurnedCDC.setText("-----------");
        textViewBurnedCDC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewBurnedCDC.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewBurnedCDC);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private void fillData(List<Benchmark> products) {
        for (Benchmark product : products) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // Aggregated Date
            TextView textViewDate  = new TextView(getContext());
            textViewDate.setText(product.aggregatedDate);
            textViewDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewDate.setPadding(5, 5, 10, 0);
            tableRow.addView(textViewDate);

            // Hours Slept
            TextView textViewHours  = new TextView(getContext());
            textViewHours.setText(String.valueOf(product.hours));
            textViewHours.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewHours.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewHours);

            // Sleep CDC
            TextView textViewSleepCDC  = new TextView(getContext());
            textViewSleepCDC.setText(String.valueOf(product.cdc_sleep_status));
            textViewSleepCDC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            if (product.cdc_sleep_status.equals("Over-sleeping") || product.cdc_sleep_status.equals("Under-sleeping")) {
                textViewSleepCDC.setTextColor(Color.RED);
            }
            textViewSleepCDC.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewSleepCDC);

            // Burned
            TextView textViewBurned  = new TextView(getContext());
            textViewBurned.setText(String.valueOf(product.burned));
            textViewBurned.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewBurned.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewBurned);

            // Burned CDC
            TextView textViewBurnedCDC  = new TextView(getContext());
            textViewBurnedCDC.setText(String.valueOf(product.cdc_calories_burned_status));
            textViewBurnedCDC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            if (product.cdc_calories_burned_status.equals("Sedentary")) {
                textViewBurnedCDC.setTextColor(Color.RED);
            }
            textViewBurnedCDC.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewBurnedCDC);

            tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

        }
    }

}
