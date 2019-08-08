package com.example.healthclubapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    private TableLayout tableLayout;
    List<DailyStat> products = new ArrayList<DailyStat>();

    public DailyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily, container, false);
        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout_daily);
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
        firestore.collection("std_member").document(user.getUid()).collection("std_member_daily")
                .orderBy("aggregated_date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Document", document.getId() + " => " + document.getData());
                                String aggregated_date = "";
                                String weight = "";
                                String hours = "";
                                String calories_consumed = "";
                                String calories_burned = "";

                                if (document.get("aggregated_date") != null) { aggregated_date = document.get("aggregated_date").toString(); }
                                if (document.get("weight") != null) { weight = document.get("weight").toString(); }
                                if (document.get("hours_slept") != null) { hours = document.get("hours_slept").toString(); }
                                if (document.get("calories_consumed") != null) { calories_consumed = document.get("calories_consumed").toString(); }
                                if (document.get("calories_burned") != null) { calories_burned = document.get("calories_burned").toString(); }

                                products.add(new DailyStat( aggregated_date, weight, hours, calories_consumed, calories_burned));
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

        // Weight
        TextView textViewWeight = new TextView(getContext());
        textViewWeight.setText("Weight");
        textViewWeight.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewWeight.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewWeight);

        // Hours Slept
        TextView textViewHours = new TextView(getContext());
        textViewHours.setText("Sleep\n(Hrs)");
        textViewHours.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewHours.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewHours);

        // Calories Consumed
        TextView textViewConsumed = new TextView(getContext());
        textViewConsumed.setText("Consumed\n(kCal)");
        textViewConsumed.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewConsumed.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewConsumed);

        // Calories Consumed
        TextView textViewBurned = new TextView(getContext());
        textViewBurned.setText("Burned\n(kCal)");
        textViewBurned.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewBurned.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewBurned);

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

        // Weight
        textViewWeight = new TextView(getContext());
        textViewWeight.setText("-----------");
        textViewWeight.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewWeight.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewWeight);

        // Hours Slept
        textViewHours = new TextView(getContext());
        textViewHours.setText("-----------");
        textViewHours.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewHours.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewHours);

        // Consumed
        textViewConsumed = new TextView(getContext());
        textViewConsumed.setText("-----------");
        textViewConsumed.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewConsumed.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewConsumed);


        // Burned
        textViewBurned = new TextView(getContext());
        textViewBurned.setText("-----------");
        textViewBurned.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewBurned.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewBurned);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private void fillData(List<DailyStat> products) {
        for (DailyStat product : products) {
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

            // Weight
            TextView textViewWeight = new TextView(getContext());
            textViewWeight.setText(product.weight);
            textViewWeight.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewWeight.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewWeight);

            // Hours Slept
            TextView textViewHours  = new TextView(getContext());
            textViewHours.setText(String.valueOf(product.hours));
            textViewHours.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewHours.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewHours);

            // Consumed
            TextView textViewConsumed = new TextView(getContext());
            textViewConsumed.setText(String.valueOf(product.consumed));
            textViewConsumed.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewConsumed.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewConsumed);

            // Burned
            TextView textViewBurned  = new TextView(getContext());
            textViewBurned.setText(String.valueOf(product.burned));
            textViewBurned.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textViewBurned.setPadding(5, 5, 5, 0);
            tableRow.addView(textViewBurned);

            tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
        }
    }

}
