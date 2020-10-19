package com.example.group_w01_07_3.features.discover;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.history.OpenedCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.LocationUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Response;

public class popUpWindow {
    private PopupWindow popupWindow;

    JSONObject selectedCapsule = new JSONObject();
    public popUpWindow(JSONObject selectedCapsule) {
        this.selectedCapsule = selectedCapsule;
        Log.d("POPUPWINDOW", "selectedCapsule: " + selectedCapsule);
    }

    //combine with discover capsule class later
    public void createWindow(final View view, final JSONObject capsuleInfo) throws JSONException {
        LocationUtil locationUtil=new LocationUtil((AppCompatActivity) view.getContext());
        Location location=locationUtil.getLocation();
        final JSONObject request=new JSONObject();
        request.put("lat",location.getLatitude());
        request.put("lon",location.getLongitude());
        request.put("time", Calendar.getInstance().getTime());
        request.put("tkn",capsuleInfo.get("tkn"));
        request.put("cid",capsuleInfo.get("cid"));
        //add two additional information:tkn and cid after the discovery activity has been finished.
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_window_layout, null);
        Intent intent=new Intent(view.getContext(), OpenedCapsule.class);
        view.getContext().startActivity(intent);
        TextView hint = (TextView) popupView.findViewById(R.id.hint);
        final boolean focusable = true;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Random random = new Random();
        int choice = random.nextInt() % 3;
        //choice=1;
        switch (choice) {
            //choice 1 slide bar
            case 0:
                popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                hint.setText("Slide this bar to the end of right");
                VerificationSeekBar seekBar2 = (VerificationSeekBar) popupView.findViewById(R.id.progress);
                seekBar2.setVisibility(View.VISIBLE);
                seekBar2.setOnSeekBarChangeListener(new VerificationSeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean onTouch) {
                        if (progress >= 100) {
                            seekBar.setProgress(100);
                            //Toast.makeText(popupView.getContext(),"Congraduation! You have been success!",Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(getApplicationContext(), "The seekbar Progress", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //Toast.makeText(popupView.getContext(),"seekbar touch start",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (seekBar.getProgress() < 100) {
                            seekBar.setProgress(0);
                        } else {
                            Toast.makeText(popupView.getContext(), "Congraduation! You have been success! Wait for open", Toast.LENGTH_LONG).show();
                            popupWindow.dismiss();
                            HttpUtil.openCapsule(request,new okhttp3.Callback(){
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    try {
                                        JSONObject replyJSON=new JSONObject(response.body().string());
                                        if (replyJSON.has("Success")){
                                            Intent intent=new Intent(view.getContext(), Display.class);
                                            intent.putExtra("capsule",capsuleInfo.toString());
                                            view.getContext().startActivity(intent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    Toast.makeText(view.getContext(),"The connection fail",Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    }
                });
                break;
            case 1:
                popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                hint.setText("Click the tap area to open the capsule!");
                ImageView img = (ImageView) popupView.findViewById(R.id.tap_me);
                img.setVisibility(View.VISIBLE);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        Toast.makeText(popupView.getContext(), "Click successfully!You have been success! Wait for open", Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                        HttpUtil.openCapsule(request,new okhttp3.Callback(){
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                try {
                                    JSONObject replyJSON=new JSONObject(response.body().string());
                                    if (replyJSON.has("Success")){
                                        Intent intent=new Intent(view.getContext(), Display.class);
                                        intent.putExtra("capsule",capsuleInfo.toString());
                                        view.getContext().startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Toast.makeText(view.getContext(),"The connection fail",Toast.LENGTH_SHORT);
                            }
                        });


                    }
                });
                break;
            case 2:
                Toast.makeText(view.getContext(), "Wait for shake listener", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
