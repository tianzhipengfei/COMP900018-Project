package com.example.group_w01_07_3.features.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.group_w01_07_3.R;
import com.squareup.picasso.Picasso;

public class DetailedCapsuleHistoryItem extends AppCompatActivity {

    private Toolbar mToolbar;

    TextView title;
    TextView date;
    TextView tag;
    TextView content;
    TextView username;
    ImageView image;
    ImageView avatar;


    String titleString,dateString,tagString,usernameString,contentString;
    String imageLocation, avatarLocation;

    OpenedCapsule item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Define Transition, used specifically during shared element transition
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Fade fade = new Fade();
        window.setEnterTransition(fade);
        window.setExitTransition(fade);
        window.setAllowEnterTransitionOverlap(false);
        window.setAllowReturnTransitionOverlap(false);

        setContentView(R.layout.activity_detailed_capsule_history_item);

        // Postpone the transition until the window's decor view has finished its layout.
        // Must include these otherwise status bar/background/toolbar will blink when entering
        postponeEnterTransition();

        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });

        //set up toolbar
        mToolbar = findViewById(R.id.detail_history_capsule_back_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Capsule Review");

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailedCapsuleHistoryItem.super.onBackPressed();
            }
        });


        title = findViewById(R.id.history_detail_title);
        date = findViewById(R.id.history_detail_date);

        image = findViewById(R.id.history_detail_image);
        tag = findViewById(R.id.history_detail_capsule_private_public_tag);
        content = findViewById(R.id.history_detail_content);

        avatar = findViewById(R.id.history_detail_capsule_original_user_avatar);

        username = findViewById(R.id.history_detail_username);

        //get the capsule object
        item = (OpenedCapsule) getIntent().getExtras().getSerializable("capsuleObject");

        loadCapsule(item);
    }


    //这里设置的部分都会被update,不管那一块写了transition没有
    private void loadCapsule(OpenedCapsule item){
        titleString  = item.getCapsule_title();
        dateString = item.getOpened_date();
        tagString = item.getTag();
        usernameString = item.getUsername();
        contentString = item.getContent();

        title.setText(titleString);
        date.setText(dateString);
        tag.setText(tagString);
        content.setText(contentString);
        username.setText(usernameString);

        imageLocation = item.getCapsule_url();
        avatarLocation = item.getAvatar_url();

        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure we call this only once
                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Picasso.with(DetailedCapsuleHistoryItem.this)
                        .load(imageLocation)
                        .resize(image.getWidth(),0)
                        .into(image);
            }
        });

        //when display the exact image, NOT allow stretch image. so fix width with auto height
//        Picasso.with(this).load(imageLocation).resize(image.getWidth(),0).into(image);
        Picasso.with(this)
                .load(avatarLocation)
                .fit()
                .into(avatar);

    }
}