package com.example.healthclubapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatasourceFragment extends Fragment {

    private FirebaseFunctions mFunctions;
    public DatasourceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_datasource, container, false);
        mFunctions = FirebaseFunctions.getInstance();
        Button buttonSync = (Button) view.findViewById(R.id.buttonSync);
        buttonSync.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadGoogleFitData();
            }
        });

        TextView textView = (TextView) view.findViewById(R.id.textViewDatasource);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setScrollBarFadeDuration(0);

        // Inflate the layout for this fragment
        return view;
    }

    private void loadGoogleFitData() {

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            Log.w("FirebaseIdToken", idToken);
                            // Send token to your backend via HTTPS

                            read_googlefit_datasources(idToken)
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                Exception e = task.getException();
                                                if (e instanceof FirebaseFunctionsException) {
                                                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                                    FirebaseFunctionsException.Code code = ffe.getCode();
                                                    Object details = ffe.getDetails();
                                                }
                                                Log.w("read_googlefit_datasources:onFailure",  e);
                                            }
                                            else {
                                                String result = task.getResult();
                                                Log.w("loadGoogleFitData", result);

                                                TextView textViewSync = (TextView) getView().findViewById(R.id.textViewDatasource);

                                                JSONObject jsonObject = null;
                                                try {
                                                    jsonObject = new JSONObject(result);
                                                    textViewSync.setText(jsonObject.toString(3));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }
                                    });
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
    }

    // [START function_from_googlefit_to_gcs]
    private Task<String> read_googlefit_datasources(String idToken) {
        // Create the arguments to the callable function.
        JSONObject data = new JSONObject();
        try{
            data.put("idtoken", idToken);
            data.put("accesstoken", MainActivity.accessToken);
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.w("request_data", data.toString());

        return mFunctions
                .getHttpsCallable("read_googlefit_datasources")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        HashMap hashMap = (HashMap) task.getResult().getData();
                        String result = new JSONObject(hashMap).toString();
                        Log.w("read_googlefit_datasources", result);
                        return result;
                    }
                });
    }
    // [END function_add_message]

}
