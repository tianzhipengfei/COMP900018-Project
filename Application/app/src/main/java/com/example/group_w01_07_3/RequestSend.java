package com.example.group_w01_07_3;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class RequestSend extends AsyncTask<String,String,String> {
    @Override
    protected String doInBackground(String... strings) {
        Log.d("send the request", "doInBackground: ");
        OkHttpClient client=new OkHttpClient();
        String dburl="https://www.tianzhipengfei.xin/mobile/createCapsule";
        FormBody body=new FormBody.Builder().build();
        Request request;
        Log.d(TAG, "doInBackground: Sending request");
        try{
            RequestBody requestBody= new FormBody.Builder()
                    .add("token",strings[0])
                    .add("title",strings[2])
                    .add("content",strings[1])
                    .add("lat",strings[3])
                    .add("lon",strings[4])
                    .add("time",strings[5])
                    .add("permission",strings[6])
                    .build();
            request=new Request.Builder().url(dburl).post(requestBody).build();
            Response response=client.newCall(request).execute();
            String res=response.body().string();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Sending request", "doInBackground: "+"There is exception");
        }
        return null;
    }
    @Override
    protected void onPostExecute(String s) {
        Log.d("Result", "onPostExecute result: "+s);
    }
}
