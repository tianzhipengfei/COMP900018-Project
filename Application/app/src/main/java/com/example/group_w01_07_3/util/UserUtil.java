package com.example.group_w01_07_3.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserUtil {

    /**
     * store user token by using CaesarCipher
     * @param context
     * @param token
     */
    public static void setToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", CaesarCipherUtil.encrypt(token, 77));
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        String token = CaesarCipherUtil.decrypt(pref.getString("token", ""), 77);
        return token;
    }

    public static void clearToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

}