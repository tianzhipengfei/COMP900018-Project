package com.example.group_w01_07_3.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * HttpUtil uses Http protocol to access server
 */
public class HttpUtil {

    // media type
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType JPG = MediaType.parse("image/jpg");
    private static final MediaType AAC = MediaType.parse("audio/aac");

    // server address and smmsURL
    private static String address = "https://www.tianzhipengfei.xin/mobile/";
    private static String smmsURL = "https://sm.ms/api/v2/upload";

    // network connection timeout second
    private static int POST_TIMEOUT_SECOND = 5;
    private static int UPLOAD_TIMEOUT_SECOND = 15;

    /**
     * user sign up
     *
     * @param paras    user information: username, password, email address, date of birth, avatar (optional)
     * @param callback callback the response
     */
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
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "signUp")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * upload avatar
     *
     * @param username   username
     * @param avatarFile avatar file
     * @param callback   callback the response
     */
    public static void uploadAvatar(String username, File avatarFile, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(UPLOAD_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("format", username)
                .addFormDataPart("smfile", avatarFile.getName(), RequestBody.create(avatarFile, HttpUtil.JPG))
                .build();
        Request request = new Request.Builder()
                .url(HttpUtil.smmsURL)
                .header("Authorization", "P6cnD1swearDYSV4bY7Y9eY836efVyUt")
                .header("Content-Type", "multipart/form-data")
                .header("User-Agent", "PostmanRuntime/7.26.5")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * user sign in
     *
     * @param username username
     * @param password password
     * @param callback callback the response
     */
    public static void signIn(String username, String password, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("usr", username);
            json.put("pwd", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "signIn")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * user sign out
     *
     * @param token    user token
     * @param callback callback the response
     */
    public static void signOut(String token, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("tkn", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "signOut")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * get user profile
     *
     * @param token    user token
     * @param callback callback the response
     */
    public static void getProfile(String token, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(HttpUtil.address + "getProfile?tkn=" + token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * get capsule
     *
     * @param token       user token
     * @param capsuleInfo capsule information
     * @param callback    callback the response
     * @throws JSONException JSONException
     */
    public static void getCapsule(String token, JSONObject capsuleInfo, okhttp3.Callback callback) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(HttpUtil.address + "discoverCapsule?" + "tkn=" + token
                        + "&lat=" + capsuleInfo.get("lat")
                        + "&lon=" + capsuleInfo.get("lon")
                        + "&max_distance=10&min_distance=0.5&num_capsules=20")
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * upload image
     *
     * @param token      user token
     * @param avatarFile avatar file or image file
     * @param callback   callback the response
     */
    public static void uploadImage(String token, File avatarFile, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(UPLOAD_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("format", "jpg")
                .addFormDataPart("smfile", avatarFile.getName(), RequestBody.create(avatarFile, HttpUtil.JPG))
                .build();
        Request request = new Request.Builder()
                .url(HttpUtil.smmsURL)
                .header("Authorization", "P6cnD1swearDYSV4bY7Y9eY836efVyUt")
                .header("Content-Type", "multipart/form-data")
                .header("User-Agent", "PostmanRuntime/7.26.5")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * change user avatar
     *
     * @param token    user token
     * @param avatar   avatar link
     * @param callback callback the response
     */
    public static void changeAvatar(String token, String avatar, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("tkn", token);
            json.put("avatar", avatar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "changeAvatar")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * create capsule
     *
     * @param capsuleInfo capsule information
     * @param callback    callback the response
     */
    public static void createCapsule(JSONObject capsuleInfo, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(capsuleInfo.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "createCapsule")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * change user password
     *
     * @param token       user token
     * @param oldPassword old password
     * @param newPassword new password
     * @param callback    callback the response
     */
    public static void changePassword(String token, String oldPassword, String newPassword, okhttp3.Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("tkn", token);
            json.put("oldpass", oldPassword);
            json.put("newpass", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(json.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "changePassword")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * open capsule
     *
     * @param requestInfo request information
     * @param callback    callback the response
     */
    public static void openCapsule(JSONObject requestInfo, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(requestInfo.toString(), HttpUtil.JSON);
        Request request = new Request.Builder()
                .url(HttpUtil.address + "openCapsule")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * upload audio
     *
     * @param token     user token
     * @param audioFile audio file
     * @param callback  callback the response
     */
    public static void uploadAudio(String token, File audioFile, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(UPLOAD_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tkn", token)
                .addFormDataPart("myfile", audioFile.getName(),
                        RequestBody.create(audioFile, HttpUtil.AAC))
                .build();
        Request request = new Request.Builder()
                .url(HttpUtil.address + "uploadAudio")
                .header("enctype", "multipart/form-data")
                .header("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * check online or not
     *
     * @param activity activity
     * @return is online or not
     */
    public static boolean isOnline(AppCompatActivity activity) {
        ConnectivityManager cm;
        cm = (ConnectivityManager) activity.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * check the network connection
     *
     * @param context context
     * @return is network connected or not
     */
    public static boolean isNetworkConnected(Context context) { // Check whether user connects to Internet or not
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * get opened capsule history
     *
     * @param token    user token
     * @param start    start which number of capsule
     * @param num      the number of capsule want to get
     * @param callback callback the response
     */
    public static void getHistory(String token, int start, int num, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(POST_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(HttpUtil.address + "getCapsuleHistory?tkn=" + token + "&start=" +
                        String.valueOf(start) + "&num=" + String.valueOf(num))
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }
}