package com.example.group_w01_07_3.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

/**
 * MessageUtil is used to create Toast and Snackbar message
 */
public class MessageUtil {

    private static Toast toast; // Toast
    private static Snackbar snackbar; // Snackbar

    /**
     * Display toast in a non-overlap manner
     *
     * @param context The context which toast will display at
     * @param content The message to display
     * @param length  the duration of toast display
     */
    public static void displayToast(Context context, String content, int length) {
        if (toast == null) {
            toast = Toast.makeText(context, content, length);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * Display snackbar in a non-overlap manner
     *
     * @param view   view where snackbar will display at
     * @param msg    the message to display
     * @param length the duration of snackbar display
     */
    public static void displaySnackbar(View view, String msg, int length) {
        if (snackbar == null || !snackbar.getView().isShown()) {
            snackbar = Snackbar.make(view, msg, length);
            snackbar.show();
        }
    }

}