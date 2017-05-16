package com.westproject.boot3.pocketsprinter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class loginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {


    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    private static final String TAG = "loginActivity";
    private static final int RC_SIGN_IN = 0001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase
        mAuth = FirebaseAuth.getInstance();

        //Views
        mStatusTextView = (TextView) findViewById(R.id.status);


        //Listeners for buttons
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        // Declare options to declare what parts of the google API you want to make use of.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(loginActivity.this.getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Create the actual client to connect to the google API with the options declared above.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    //Method for handling cases for different buttons
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                signIn();
                break;
        }

    }


    //Method  for signing in
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //If no connection can be made
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    // Return result from signIn()
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, String.valueOf(RC_SIGN_IN)+result.getStatus());
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Log.d(TAG,"SUCCESS");
            } else {
                Log.d(TAG,"ERROR");
            }
        }
    }


    // [START auth_with_google]


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
              mStatusTextView.setText("we good");
            firebaseAuthWithGoogle(acct);



        } else {

            // Signed out, show unauthenticated UI.

          //  updateUI(false);

        }

    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);

            findViewById(R.id.to_menu_button).setVisibility(View.VISIBLE);

        } else {

            //     mStatusTextView.setText(R.string.signed_out);


            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);

            findViewById(R.id.to_menu_button).setVisibility(View.GONE);

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle"+ acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "SignInWithCredential:Success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                    }
                });
    }


}
