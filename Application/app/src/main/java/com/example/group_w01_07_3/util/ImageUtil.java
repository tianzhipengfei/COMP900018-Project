package com.example.group_w01_07_3.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ImageUtil is useful to compress image and get image bitmap from Http image url
 */
public class ImageUtil {

    private static Bitmap urlBitmap; // image bitmap from Http image url

    /**
     * compress image
     *
     * @param context  context
     * @param bitmap   image bitmap
     * @param filename image filename
     * @return compressed image file
     */
    public static File compressImage(Context context, Bitmap bitmap, String filename) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos); // compress 30%
        int options = 30;
        while (baos.toByteArray().length / 1024 > 500) {
            // loop to determine if the compressed image is larger than 500kb, and continue to compress if larger
            baos.reset(); // clear baos
            options -= 5; // every time reduce by 5
            if (options == 5){
                break;
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos); // compress options% and store the compressed data in baos
            }
        }
        File file = new File(context.getExternalCacheDir(), filename); // image file
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

    /**
     * get image bitmap from Http image url
     *
     * @param urlString Http image url
     * @return image bitmap
     */
    public static Bitmap getHttpImage(String urlString) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlString)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() { // get image bitmap from Http image url
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                urlBitmap = BitmapFactory.decodeStream(inputStream);
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
        return urlBitmap;
    }

}