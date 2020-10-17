package com.example.group_w01_07_3.features.discover;
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

import java.util.Random;

public class popUpWindow extends AppCompatActivity {
    private PopupWindow popupWindow;
    //combine with discover capsule class later
    public void createWindow(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_window_layout, null);
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
                            popupWindow.dismiss();
                            System.out.println(popupWindow);
                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            LayoutInflater inflater = (LayoutInflater)
                                    getSystemService(LAYOUT_INFLATER_SERVICE);
                            //asychronize request, post
                            Toast.makeText(popupView.getContext(), "Congraduation! You have been success!", Toast.LENGTH_SHORT).show();
                            //new activity
                        }
                        Toast.makeText(popupView.getContext(), "seekbar touch stopped", Toast.LENGTH_SHORT).show();
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
                    public void onClick(View view) {
                        popupWindow.dismiss();
                        Toast.makeText(popupView.getContext(), "Click successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case 2:
                Toast.makeText(getApplicationContext(), "Wait for shake listener", Toast.LENGTH_SHORT);
                break;
        }
    }
}
