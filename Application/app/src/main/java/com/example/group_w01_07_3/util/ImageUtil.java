package com.example.group_w01_07_3.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageUtil {

    private static Bitmap urlBitmap;

    public static File compressImage(Context context, Bitmap bitmap, String filename) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 500) {
            baos.reset();
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        File file = new File(context.getExternalCacheDir(), filename);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static Bitmap getHttpImage(String urlString) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlString)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                urlBitmap = BitmapFactory.decodeStream(inputStream);
            }

            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
        return urlBitmap;

//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setConnectTimeout(5000);
//            connection.setReadTimeout(5000);
//            int code = connection.getResponseCode();
//            if (code == HttpURLConnection.HTTP_OK) {
//                InputStream inputStream = connection.getInputStream();
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                return bitmap;
//            }
//            connection.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
    }

}