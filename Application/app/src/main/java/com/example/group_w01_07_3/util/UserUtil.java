package com.example.group_w01_07_3.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserUtil {

    /**
     * store user token encrypted by using CaesarCipher
     *
     * @param context context
     * @param token   user token
     */
    public static void setToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", CaesarCipherUtil.encrypt(token, 77)); // encrypt
        editor.apply();
    }

    /**
     * get user token decrypted by using CaesarCipher
     *
     * @param context context
     * @return user token
     */
    public static String getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        String token = CaesarCipherUtil.decrypt(pref.getString("token", ""), 77); // decrypt
        return token;
    }

    /**
     * clear user token
     *
     * @param context context
     */
    public static void clearToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

}