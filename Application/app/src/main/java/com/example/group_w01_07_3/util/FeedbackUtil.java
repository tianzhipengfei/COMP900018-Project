package com.example.group_w01_07_3.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * FeedbackUtil is used when need vibrate feedback
 */
public class FeedbackUtil {

    private Context context;

    /**
     * constructor
     *
     * @param context context
     */
    public FeedbackUtil(Context context) {
        this.context = context;
    }

    /**
     * vibrate feedback method
     *
     * @param context context
     */
    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(200);
        }
    }

}