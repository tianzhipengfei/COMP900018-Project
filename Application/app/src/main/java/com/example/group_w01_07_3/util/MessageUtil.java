package com.example.group_w01_07_3.util;

import android.content.Context;
import android.widget.Toast;

public class MessageUtil {
    private static Toast toast;

    /**
     * Display toast in a non-overlap manner
     *
     * @param context The context which toast will display at
     * @param content     The message to display
     * @param length  the duration of toast display
     */
    public static void displayToast(Context context,
                                 String content, int length) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    length);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

}
