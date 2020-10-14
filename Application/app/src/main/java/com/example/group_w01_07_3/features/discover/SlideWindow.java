package com.example.group_w01_07_3.features.discover;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.example.group_w01_07_3.R;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SlideWindow {
    //Waiting to merge with discovery activity, just this function here to present the logic.
    public void createWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_window_layout, null);
        boolean focusable = true;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        VerificationSeekBar seekBar2 = (VerificationSeekBar) popupView.findViewById(R.id.progress);
        seekBar2.setOnSeekBarChangeListener(new VerificationSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean onTouch) {
                if (progress >= 100) {
                    seekBar.setProgress(100);
                    Toast.makeText(popupView.getContext(), "Congraduation! You have been success!", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(getApplicationContext(), "The seekbar Progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(popupView.getContext(), "seekbar touch start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() < 100) {
                    seekBar.setProgress(0);
                } else {
                    Toast.makeText(popupView.getContext(), "Congraduation! You have been success!", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(popupView.getContext(), "seekbar touch stopped", Toast.LENGTH_SHORT).show();
            }
        });
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }
}

