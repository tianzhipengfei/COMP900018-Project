package com.example.group_w01_07_3;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class BottomDialog extends Dialog implements View.OnClickListener {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private Context context;
    public static Uri imageUri;

    public BottomDialog(@NonNull Context context) {
        super(context, R.style.BottomDialog);
        this.context = context;
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
                File outputImage = new File(this.context.getExternalCacheDir(), "output_photo.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(this.context,
                            "com.example.group_w01_07_3.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                ((Activity) this.context).startActivityForResult(intent, TAKE_PHOTO);
                break;
            case R.id.dialog_gallery:
                if (ContextCompat.checkSelfPermission(this.context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) this.context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                break;
            case R.id.dialog_cancel:
                this.dismiss();
                break;
        }
    }

    public View getContentView() {
        return this.findViewById(android.R.id.content);
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        ((Activity) this.context).startActivityForResult(intent, CHOOSE_PHOTO);
    }

}