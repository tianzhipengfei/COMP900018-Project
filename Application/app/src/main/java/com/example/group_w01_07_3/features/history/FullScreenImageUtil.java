package com.example.group_w01_07_3.features.history;

import com.bumptech.glide.Glide;
import com.example.group_w01_07_3.R;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Window;
import android.widget.ImageView;

public class FullScreenImageUtil extends AppCompatActivity {
    private String imageURL;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Fade fade = new Fade();
        window.setEnterTransition(fade);
        window.setExitTransition(fade);
        window.setAllowEnterTransitionOverlap(false);
        window.setAllowReturnTransitionOverlap(false);

        setContentView(R.layout.activity_full_screen_image_util);

        imageURL = getIntent().getStringExtra("ImageURL");
        imageView = findViewById(R.id.full_screen_image);

        Glide.with(this)
                .load(imageURL)
                .into(imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}