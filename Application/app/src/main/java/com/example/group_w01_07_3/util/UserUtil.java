package com.example.group_w01_07_3.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserUtil {

    public static void setToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        //editor.putString("token", CaesarCipherUtil.encrypt(token, 77));
        editor.putString("token", token);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        //String token = CaesarCipherUtil.decrypt(pref.getString("token", ""), 77);
        String token = pref.getString("token", "");
        return token;
    }

    public static void clearToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

}