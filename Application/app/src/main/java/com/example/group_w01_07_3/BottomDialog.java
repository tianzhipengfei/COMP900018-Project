package com.example.group_w01_07_3;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

public class BottomDialog extends Dialog implements View.OnClickListener {

    public BottomDialog(@NonNull Context context) {
        super(context, R.style.BottomDialog);
        setContentView(R.layout.dialog_content_circle);
        initView();
    }

    private void initView() {
        findViewById(R.id.dialog_camera).setOnClickListener(this);
        findViewById(R.id.dialog_gallery).setOnClickListener(this);
        findViewById(R.id.dialog_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_camera:
                break;
            case R.id.dialog_gallery:
                break;
            case R.id.dialog_cancel:
                this.dismiss();
                break;
        }
    }

    public View getContentView() {
        return this.findViewById(android.R.id.content);
    }

}