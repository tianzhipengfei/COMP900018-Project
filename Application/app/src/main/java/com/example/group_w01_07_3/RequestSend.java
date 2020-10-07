package com.example.group_w01_07_3;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class RequestSend extends AsyncTask<String,String,String> {
    private static final MediaType format = MediaType.parse("application/json; charset=utf-8");
    @Override
    protected String doInBackground(String... strings) {
        Log.d("send the request", "doInBackground: ");
        OkHttpClient client=new OkHttpClient();
        String dburl="https://www.tianzhipengfei.xin/mobile/createCapsule";
        FormBody body=new FormBody.Builder().build();
        Request request;
        Log.d(TAG, "doInBackground: Sending request");
        JSONObject capsule=new JSONObject();
        try {
            capsule.put("tkn",strings[0]);
            capsule.put("content",strings[1]);
            capsule.put("title",strings[2]);
            capsule.put("time",strings[3]);
            capsule.put("lat",Double.parseDouble(strings[4]));
            capsule.put("lon",Double.parseDouble(strings[5]));
            capsule.put("permission",Integer.parseInt(strings[6]));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            RequestBody requestBody=RequestBody.create(capsule.toString(),RequestSend.format);
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
