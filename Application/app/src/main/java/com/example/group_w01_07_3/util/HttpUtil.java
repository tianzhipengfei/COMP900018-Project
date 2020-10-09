package com.example.group_w01_07_3.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM_DATA = MediaType.parse("multipart/form-data");
    private static String address = "https://www.tianzhipengfei.xin/mobile/";

    public static void signUp(String[] paras, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        if (paras[4] == null) {
            try {
                json.put("usr", paras[0]);
                json.put("pwd", paras[1]);
                json.put("email", paras[2]);
                json.put("dob", paras[3]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                json.put("usr", paras[0]);
                json.put("pwd", paras[1]);
                json.put("email", paras[2]);
                json.put("dob", paras[3]);
                json.put("avatar", paras[4]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "signUp")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void uploadAvatar(String username, File avatarFile, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("usr", username)
                .addFormDataPart("myfile", avatarFile.getName(), RequestBody.create(avatarFile, FORM_DATA))
                .build();
        Request request = new Request.Builder()
                .url(HttpUtil.address + "uploadAvatar")
                .header("enctype", "multipart/form-data")
                .header("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void signIn(String username, String password, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("usr", username);
            json.put("pwd", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "signIn")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void signOut(String token, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("tkn", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "signOut")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

}