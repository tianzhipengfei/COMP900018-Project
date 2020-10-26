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
        OpenedCapsule item = (OpenedCapsule) getIntent().getExtras().getSerializable("capsuleObject");

        loadCapsule(item);
    }


    //这里设置的部分都会被update,不管那一块写了transition没有
    private void loadCapsule(OpenedCapsule item){
        String titleString  = item.getCapsule_title();
        String dateString = item.getOpened_date();
        title.setText(titleString);
        date.setText(dateString);

        String imageLocation = item.getCapsule_url(); //TODO: This is for testing purpose only. 真正实现的时候你应该用的是Picasso或者Glide从URL load
        Picasso.with(this).load(imageLocation).fit().centerInside().into(image);
//        Picasso.with(this).load(imageLocation).resize(512,512).into(image);

        String tagString = item.getTag();
        tag.setText(tagString);

        String contentString = item.getContent();
        content.setText(contentString);

        String avatarLocation = item.getAvatar_url(); //TODO: This is for testing purpose only. 真正实现的时候你应该用的是Picasso或者Glide从URL load
        Picasso.with(this).load(avatarLocation).fit().centerInside().into(avatar);
//        Picasso.with(this).load(avatarLocation).resize(48,48).into(avatar);

        String usernameString = item.getUsername();
        username.setText(usernameString);
    }
}