package com.westproject.boot3.pocketsprinter;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Gregor on 17/05/2017.
 * This class provides a userUID with a prompt to send an error long whenever the application crashes
 * It extends the application and handles any uncaught exception by sending a full crash log
 * to Mikey0812@gmail.com
 */


public class CrashReport extends Application {

    private static final String TAG = "CrashReport";

    @Override
    public void onCreate() {
        super.onCreate();
        //If an uncaught exception is found, do handleUncaughtException()
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                handleUncaughtException(thread, ex);

            }
        });
    }

    //Prompt userUID to send an email to mikey0812@gmail.com when an error occurs.
    public void handleUncaughtException (Thread thread, Throwable e)
    {
        String stackTrace = Log.getStackTraceString(e);
        String message = e.getMessage();
        Log.d(TAG, "Trying to send email");


        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"mikey0812@gmail.com"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "PocketSprinter Crash Report");
        intent.putExtra (Intent.EXTRA_TEXT, stackTrace);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }




}
