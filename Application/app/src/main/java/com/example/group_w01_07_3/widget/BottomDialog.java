package com.example.group_w01_07_3.widget;

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

import com.example.group_w01_07_3.R;

import java.io.File;
import java.io.IOException;

/**
 * BottomDialog is a dialog used to take a photo and choose a photo from the gallery
 */
public class BottomDialog extends Dialog implements View.OnClickListener {

    public static final int TAKE_PHOTO = 1; // case take photo
    public static final int CHOOSE_PHOTO = 2; // case choose photo
    public static Uri imageUri; // store image Uri
    private Context context; // store context

    public BottomDialog(@NonNull Context context) {
        super(context, R.style.BottomDialog);
        setContentView(R.layout.dialog_content_circle);

        this.context = context;

        initView();
    }

    /**
     * initialize the view
     */
    private void initView() {
        findViewById(R.id.dialog_camera).setOnClickListener(this);
        findViewById(R.id.dialog_gallery).setOnClickListener(this);
        findViewById(R.id.dialog_cancel).setOnClickListener(this);
    }

    /**
     * click which view: camera, gallery, cancel
     *
     * @param view camera, gallery, cancel
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_camera: // take a photo
                File outputImage = new File(this.context.getExternalCacheDir(), "output_photo.jpg"); // image file
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) { // version check
                    imageUri = FileProvider.getUriForFile(this.context,
                            "com.example.group_w01_07_3.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                ((Activity) this.context).startActivityForResult(intent, TAKE_PHOTO);
                break;
            case R.id.dialog_gallery: // choose a photo from gallery
                // get permission
                if (ContextCompat.checkSelfPermission(this.context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) this.context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                break;
            case R.id.dialog_cancel: // cancel
                dismiss();
                break;
        }
    }

    /**
     * get the bottom dialog content view
     *
     * @return content view
     */
    public View getContentView() {
        return findViewById(android.R.id.content);
    }

    /**
     * open album to choose a photo
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        ((Activity) this.context).startActivityForResult(intent, CHOOSE_PHOTO);
    }

}