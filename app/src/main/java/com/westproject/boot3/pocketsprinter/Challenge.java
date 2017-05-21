package com.westproject.boot3.pocketsprinter;

import android.content.Context;
import android.util.Log;

/**
 * Created by Gregor on 17/05/2017.
 * This class provides the application with challenges that get
 * progressively harder each week.
 * Descriptions are found in Resources/Values/Strings.xml
 */
public class Challenge {
    private final Workout[] workouts;
    private static final String TAG = "Challenge";

    public Challenge(Workout[] workouts) {
        this.workouts = workouts;
    }

    public Workout get(int i) {
        if (i < 0 || i >= length()) {
            throw new IndexOutOfBoundsException();
        }
        return workouts[i];
    }

    public int length() {
        return workouts.length;
    }

    //A workout
    public static class Workout {
        private final String title;
        private final String description;
        private final Segment[] segments;
        private final long[] subLengths;
        private final long totalLength;

        private Workout(String title, String description, Segment[] segments) {
            this.title = title;
            this.description = description;
            this.segments = segments;
            subLengths = new long[segments.length];
            subLengths[0] = 0;


            for (int i = 1; i < segments.length; ++i) {
                this.subLengths[i] = this.subLengths[i - 1] + segments[i - 1].length();
            }
            totalLength = subLengths[subLengths.length - 1] + segments[segments.length - 1].length();
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int numberSegments() {
            return segments.length;
        }

        public Segment getSegment(int i) {
            if (i < 0 || i >= numberSegments()) {
                throw new IndexOutOfBoundsException();
            }
            return segments[i];
        }

        public long getLengthUpTo(int i) {
            if (i == numberSegments()) {
                return totalLength;
            }
            if (i < 0 || i > numberSegments()) {
                throw new IndexOutOfBoundsException();
            }
            return subLengths[i];
        }

        public long getLength() {
            return totalLength;
        }
    }

    public enum Type {
        WARMUP,
        WALK,
        JOG;

        public String toString(Context ctx) {
            final int resId;
            switch (this) {
                case WARMUP:
                    resId = R.string.warmup_label;
                    break;
                case WALK:
                    resId = R.string.walk_label;
                    break;
                case JOG:
                    resId = R.string.jog_label;
                    break;
                default:
                    Log.wtf(TAG,"THIS IS NOT SUPPOSED TO HAPPEN PLEASE STOP HAPPENING WHAT THE FUCK IS GOING ON WITH THIS SHITTY FUNCTION OH MY GOD I SHOULD'VE NEVER MADE YOU");
                    throw new IllegalStateException("kill yourself stupid method fuck ur shit");
            }
            return ctx.getString(resId);
        }
    }

    //The segment of the progress bars
    public static class Segment {
        private Type type;
        private long lengthInMillis; // in ms

        public Segment(Type type, long lengthInMillis) {
            this.type = type;
            this.lengthInMillis = lengthInMillis;
        }

        public Type getType() {
            return type;
        }

        public long length() {
            return lengthInMillis;
        }
    }


    private static Challenge runningWorkouts;

    public static synchronized Challenge loadWorkouts(Context ctx) {
        if (runningWorkouts != null) {
            return runningWorkouts;
        }
        String[] workoutTitles = ctx.getResources().getStringArray(R.array.workoutTitles);
        String[] workoutDescriptions = ctx.getResources().getStringArray(R.array.workoutDescriptions);
        Workout[] workouts = new Workout[workoutTitles.length];
        for (int i = 0; i < workouts.length; ++i) {
            workouts[i] = new Workout(workoutTitles[i], workoutDescriptions[i], workoutSegmentLength[i]);
        }
        return runningWorkouts = new Challenge(workouts);
    }


    private static Segment S(Type type, long lengthInSecs) {
        return new Segment(type, lengthInSecs * 1000);
    }

    private static final Segment[][] workoutSegmentLength;

    //The workouts
    static {
        Segment[] testWorkout = {S(Type.WARMUP, 10), S(Type.JOG, 8), S(Type.WALK, 8), S(Type.JOG, 8)};

        Segment[] week1 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90),
                S(Type.JOG, 60),
                S(Type.WALK, 90)};
        
        Segment[] week2 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 90), 
                S(Type.WALK, 120),
                S(Type.JOG, 90),
                S(Type.WALK, 120), 
                S(Type.JOG, 90),
                S(Type.WALK, 120),
                S(Type.JOG, 90),
                S(Type.WALK, 120),
                S(Type.JOG, 90),
                S(Type.WALK, 120),
                S(Type.JOG, 90),
                S(Type.WALK, 60)};
        
        Segment[] week3 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 90), 
                S(Type.WALK, 90),
                S(Type.JOG, 180),
                S(Type.WALK, 180),
                S(Type.JOG, 90),
                S(Type.WALK, 90), 
                S(Type.JOG, 180),
                S(Type.WALK, 180)};
        
        Segment[] week4 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 180), 
                S(Type.WALK, 90),
                S(Type.JOG, 300),
                S(Type.WALK, 150),
                S(Type.JOG, 180),
                S(Type.WALK, 90),
                S(Type.JOG, 300)};
        
        Segment[] w5d1 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 300),
                S(Type.WALK, 180), 
                S(Type.JOG, 300),
                S(Type.WALK, 180),
                S(Type.JOG, 300)};
        
        Segment[] w5d2 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 480),
                S(Type.WALK, 300),
                S(Type.JOG, 480)};
        
        Segment[] w5d3 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 1200)};
        
        Segment[] w6d1 = {
                S(Type.WARMUP, 300), 
                S(Type.JOG, 300), 
                S(Type.WALK, 180),
                S(Type.JOG, 480),
                S(Type.WALK, 180),
                S(Type.JOG, 300)};
        
        Segment[] w6d2 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 600), 
                S(Type.WALK, 180),
                S(Type.JOG, 600)};
        
        Segment[] w6d3 = {
                S(Type.WARMUP, 300), 
                S(Type.JOG, 1320)};
        
        Segment[] week7 = {
                S(Type.WARMUP, 300), 
                S(Type.JOG, 1500)};
        
        Segment[] week8 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 1680)};
        
        Segment[] week9 = {
                S(Type.WARMUP, 300),
                S(Type.JOG, 1800)};
        
        workoutSegmentLength = new Segment[][]{
                testWorkout,
                week1, //*/
                week1,
                week1,
                week2,
                week2,
                week2,
                week3,
                week3,
                week3,
                week4,
                week4,
                week4,
                w5d1,
                w5d2,
                w5d3,
                w6d1,
                w6d2,
                w6d3,
                week7,
                week7,
                week7,
                week8,
                week8,
                week8,
                week9,
                week9,
                week9
        };
    }
}
