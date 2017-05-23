package com.westproject.boot3.pocketsprinter;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
* This activity provides the screen where the run timer is displayed. It also provides progress bars
* and navigation functionality through the challenges provided.

 */

public class runningActivity extends AppCompatActivity implements SensorEventListener {
    /**
     * Created by Gregor on 17/05/2017.
     * This class provides a userUID with a prompt to send an error long whenever the application crashes
     * It extends the application and handles any uncaught exception by sending a full crash log
     * to Mikey0812@gmail.com
     */
    private static final String SAVE_PROGRESS = "com.westproject.boot3.pocketsprinter";
    private static final String TAG = "runningActivity";

    private runningTimer runningTimer;
    private Challenge currentWorkoutSet;
    private int workoutCatalog;
    private int segmentIdx;

    private TextView tvCurrentWorkOut;
    private TextView tvCurrentWorkOutDescription;
    private Button btnBack;
    private Button btnForward;
    private Button btnStartPause;
    private TextView tvCountingUp;
    private TextView tvCountingDown;
    private ProgressBar pbProgressBar;
    private TextView tvCurrentActionPhase;
    private TextView tvCountingUpTotal;
    private TextView tvCountingDownTotal;
    private ProgressBar pbTotalProgressBar;
    private FirebaseDatabase mFirebaseDataBase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private SensorManager sensorManager;
    private TextView tvStepCount;
    boolean activityRunning;
    String userID;
    int newValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        setContentView(R.layout.activity_timer);

        //Prevent the screen from darkening/going to sleep if application is on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        tvCurrentWorkOut = (TextView) findViewById(R.id.title);
        tvCurrentWorkOutDescription = (TextView) findViewById(R.id.description);
        btnBack = (Button) findViewById(R.id.back);
        btnForward = (Button) findViewById(R.id.forth);
        btnStartPause = (Button) findViewById(R.id.startPause);
        tvCountingUp = (TextView) findViewById(R.id.counting_up);
        tvCountingDown = (TextView) findViewById(R.id.counting_down);
        pbProgressBar = (ProgressBar) findViewById(R.id.timer_bar);
        tvCurrentActionPhase = (TextView) findViewById(R.id.current_action);
        tvCountingUpTotal = (TextView) findViewById(R.id.counting_up_total);
        tvCountingDownTotal = (TextView) findViewById(R.id.counting_down_total);
        pbTotalProgressBar = (ProgressBar) findViewById(R.id.total_bar);

        tvStepCount = (TextView) findViewById(R.id.step_counter);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        //Database declarations
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDataBase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDataBase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                } else {

                    // User is signed out

                    Log.d(TAG, "onAuthStateChanged:signed_out");


                }
            }
        };

        currentWorkoutSet = Challenge.loadWorkouts(this);
        workoutCatalog = getPreferences(MODE_PRIVATE).getInt(SAVE_PROGRESS, 0);
        setUpCurrentWorkout();
    }


    //Method for creating a menu for future options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //Items for in the menu
    //TODO 03: Add more options, such as making vibrations optional.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Pad3_website:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PAD3_link))));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Method for pausing and resuming the timer.
    public void startPause(View view) {
        if (runningTimer.isPaused()) {
            tvCurrentActionPhase.setText(currentWorkoutSet.get(workoutCatalog).getSegment(segmentIdx).getType().toString(this));
            btnStartPause.setText(getString(R.string.pause));
            runningTimer.start();
            disableBackForthButtons();
        } else {
            runningTimer.pause();
            btnStartPause.setText(getString(R.string.start));
            cancelVibrations();
            enableBackForthButtons();
        }
    }

    //Method for setting the workout
    private void setUpCurrentWorkout() {
        segmentIdx = 0;
        tvCurrentWorkOut.setText(currentWorkoutSet.get(workoutCatalog).getTitle());
        tvCurrentWorkOutDescription.setText(Html.fromHtml(currentWorkoutSet.get(workoutCatalog).getDescription()));
        btnStartPause.setText(getString(R.string.start));
        setBackForthButtonsForCurrentWorkout();
        setUpProgressBarsForCurrentSegment();
        setUpTimerForCurrentSegment();
    }

    //Method for setting up the timer.
    private void setUpTimerForCurrentSegment() {
        if (runningTimer != null) {
            runningTimer.cancel();
        }
        runningTimer = makeTimer();
    }


    //Select next workout.
    public void nextWorkout(View view) {
        cancelVibrations();
        ++workoutCatalog;
        saveWorkoutProgress(workoutCatalog);
        setUpCurrentWorkout();
    }

    //Select previous workout
    public void prevWorkout(View view) {
        cancelVibrations();
        --workoutCatalog;
        saveWorkoutProgress(workoutCatalog);
        setUpCurrentWorkout();
    }

    //Method used to save progress to the preferences.
    private void saveWorkoutProgress(int idx) {
        getPreferences(MODE_PRIVATE).edit().putInt(SAVE_PROGRESS, idx).apply();
    }


    //Method to create the timer.
    private runningTimer makeTimer() {
        return new runningTimer(currentWorkoutSet.get(workoutCatalog).getSegment(segmentIdx).length(), 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                setProgressBars(millisUntilFinished);
                Punten();
                if (millisUntilFinished < 1000 * vibratingReminders) {
                    --vibratingReminders;
                    vibrate(warningPattern);
                }
            }


            //Method for handling finishing albeit the current segment or the total bar
            @Override
            public void onFinish() {
                vibrate(endSegmentPattern);
                if (segmentIdx < currentWorkoutSet.get(workoutCatalog).numberSegments() - 1) {
                    // When the segment finishes.
                    ++segmentIdx;
                    setUpProgressBarsForCurrentSegment();
                    setUpTimerForCurrentSegment();
                    runningTimer.start();
                } else {
                    // When the workout finishes.
                    setProgressBars(0);
                    btnStartPause.setText(getString(R.string.start));
                    enableBackForthButtons();
                    saveWorkoutProgress(workoutCatalog + 1);
                    segmentIdx = 0;
                    setUpTimerForCurrentSegment();
                    Log.d(TAG, loginActivity.getUserUID());
                     saveData();

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                        //    showData(dataSnapshot);


                        }


                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });


                }
            }


            private final long[] endSegmentPattern = {500, 110, 500, 110, 450, 110, 200, 110, 170, 40, 450, 110, 200, 110, 170, 40, 500};
            private final long[] warningPattern = {0, 150};
            private int vibratingReminders = Math.min(5, (int) (timerLife / 1000));
        };
    }

    private void saveData() {
        User user = new User();

                newValue = newValue + 100;
                user.setPunten(newValue);
            myRef.child(userID).child("Punten").setValue(user);

    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            User user = new User();
            Log.d(TAG, userID);
            try {
                user.setPunten(ds.child(userID).child("Punten").getValue(User.class).getPunten());
                newValue = user.getPunten();
            } catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
            Log.d(TAG, String.valueOf(user.getPunten()));


        }
    }
    //Setting up the segment progress bar
    private void setUpProgressBarsForCurrentSegment() {
        Challenge.Segment currentSegment = currentWorkoutSet.get(workoutCatalog).getSegment(segmentIdx);
        tvCurrentActionPhase.setText(currentSegment.getType().toString(runningActivity.this));
        setProgressBars(currentSegment.length());
    }


    private void Punten() {
        Challenge.Segment currentSegment = currentWorkoutSet.get(workoutCatalog).getSegment(segmentIdx);
        Log.d(TAG, currentSegment.getType().toString(runningActivity.this));

        String currentExercise = (currentSegment.getType().toString(runningActivity.this));
        switch (currentExercise) {
            case "Warm Up":
                sensorManager.unregisterListener(this);
                break;
            case "Walk":
                sensorManager.unregisterListener(this);
                break;
            case "Jogging":
                onResume();
                break;

        }
    }

    //Filling in the progressbar.
    private void setProgressBars(long millisToEnd) {
        long localTotal = currentWorkoutSet.get(workoutCatalog).getSegment(segmentIdx).length();
        pbProgressBar.setProgress((int) (((localTotal - millisToEnd) * 100) / localTotal));
        tvCountingUp.setText(convertToMMSS(localTotal - millisToEnd));
        tvCountingDown.setText(String.format(Locale.US, "-%s", convertToMMSS(millisToEnd + 999)));

        long totalElapsed = currentWorkoutSet.get(workoutCatalog).getLengthUpTo(segmentIdx) + (localTotal - millisToEnd);
        long workoutLength = currentWorkoutSet.get(workoutCatalog).getLength();
        pbTotalProgressBar.setProgress((int) ((totalElapsed * 100) / workoutLength));
        tvCountingUpTotal.setText(convertToMMSS(totalElapsed));
        tvCountingDownTotal.setText(String.format(Locale.US, "-%s", convertToMMSS(workoutLength - totalElapsed + 999)));
    }

    //Creating the back and forth buttons.
    private void setBackForthButtonsForCurrentWorkout() {
        btnForward.setVisibility((workoutCatalog < currentWorkoutSet.length() - 1) ? View.VISIBLE : View.INVISIBLE);
        btnBack.setVisibility((workoutCatalog > 0) ? View.VISIBLE : View.INVISIBLE);
        enableBackForthButtons();
    }

    //Enabling the buttons
    private void enableBackForthButtons() {
        btnForward.setEnabled(true);
        btnBack.setEnabled(true);
    }

    //Disabling the buttons
    private void disableBackForthButtons() {
        btnForward.setEnabled(false);
        btnBack.setEnabled(false);
    }

    private static SimpleDateFormat df = new SimpleDateFormat("m:ss", Locale.ENGLISH);

    static {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static String convertToMMSS(long millis) {
        return df.format(new Date(millis));
    }

    //vibrate now damnit
    private void vibrate(long[] pattern) {
        final Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.vibrate(pattern, -1, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
        } else {
            v.vibrate(pattern, -1);
        }
    }


    //stop vibrations right now damnit
    private void cancelVibrations() {
        final Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (activityRunning) {
            tvStepCount.setText(String.valueOf(event.values[0]));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister t1
        // 1
        // 1he last listener, the hardware will stop detecting step events
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updatePoints(String userID, int points) {


    }
}
