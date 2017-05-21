package com.westproject.boot3.pocketsprinter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

/**
 * Created by Delug on 16/05/2017.
 */

public class menuActivity extends AppCompatActivity {

    private CalendarView mCalenderView;

    private static final String TAG = "menuActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        Log.d(TAG, "onCreate: Started");
        Button button =(Button) findViewById(R.id.btnGoToCounter);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(menuActivity.this, runningActivity.class);
                startActivity(intent);
            }
        });

        mCalenderView = (CalendarView) findViewById(R.id.calendarView);


        mCalenderView.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){

            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {


            }
        });




    }
}
