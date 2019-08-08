package com.example.healthclubapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.transform.Result;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    public static String accessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn();
            }
        });

        final Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });
    }

    public void signIn() {
        // Code here executes on main thread after user presses button
        // Log.w("myApp", "Hell world");

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder()
                        .setScopes(Arrays.asList(
                                Scopes.FITNESS_ACTIVITY_READ_WRITE,
                                Scopes.FITNESS_BLOOD_GLUCOSE_READ_WRITE,
                                Scopes.FITNESS_BLOOD_PRESSURE_READ_WRITE,
                                Scopes.FITNESS_BODY_READ_WRITE,
                                Scopes.FITNESS_BODY_TEMPERATURE_READ_WRITE,
                                Scopes.FITNESS_LOCATION_READ_WRITE,
                                Scopes.FITNESS_NUTRITION_READ_WRITE,
                                Scopes.FITNESS_OXYGEN_SATURATION_READ_WRITE,
                                Scopes.FITNESS_REPRODUCTIVE_HEALTH_READ_WRITE
                                )
                        )
                        .build()
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_healthclubapp_background)
                        .setIsSmartLockEnabled(true)
                        .build(),
                RC_SIGN_IN);
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.w("myApp", user.getDisplayName());

                final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                String IdToken = account.getIdToken();
                Log.d("IdToken", "IdToken:"+ IdToken);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String scope = "oauth2:"+Scopes.EMAIL+" "+ Scopes.PROFILE;
                            String accessToken = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(), scope, new Bundle());
                            MainActivity.accessToken = accessToken;
                            Log.d("accessToken", "accessToken:"+accessToken); //accessToken:ya29.Gl...

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                        }
                    }
                };
                AsyncTask.execute(runnable);

                Intent intent = new Intent(this, Main2Activity.class);
                startActivity(intent);

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}
