package com.example.group_w01_07_3.features.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.group_w01_07_3.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

public class DetailedCapsuleHistoryItem extends AppCompatActivity {

    private Toolbar mToolbar;

    private ShimmerFrameLayout imageShimmer, avatarShimmer, voiceShimmer;

    TextView title;
    TextView date;
    TextView tag;
    TextView content;
    TextView username;
    ImageView image;
    ImageView avatar;
    ImageButton voice;


    String titleString,dateString,tagString,usernameString,contentString;
    String imageLocation, avatarLocation, voiceLocation;

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

        //find view for shimmer placeholder layout
        imageShimmer = findViewById(R.id.history_detail_shimmer_image);
        avatarShimmer = findViewById(R.id.history_detail_shimmer_avatar);
        voiceShimmer = findViewById(R.id.history_detail_shimmer_voice);

        //find view for all string content
        title = findViewById(R.id.history_detail_title);
        date = findViewById(R.id.history_detail_date);
        username = findViewById(R.id.history_detail_username);
        tag = findViewById(R.id.history_detail_capsule_private_public_tag);
        content = findViewById(R.id.history_detail_content);

        //find view for all non-string content that require loading time
        image = findViewById(R.id.history_detail_image);
        avatar = findViewById(R.id.history_detail_capsule_original_user_avatar);
        voice = findViewById(R.id.history_detail_voice);

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
        voiceLocation = item.getVoice_url();

        loadImage();
        loadAvatar();
        loadVoice();


    }

    private void loadImage(){
        //Must use  this tree observer to load content image
        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure we call this only once
                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Picasso.with(DetailedCapsuleHistoryItem.this)
                        .load(imageLocation)
                        .resize(image.getWidth(),0)
                        .into(image, new com.squareup.picasso.Callback() {

                            //once loaded, hide the placeholder shimmer, then show the user image
                            @Override
                            public void onSuccess() {
                                imageShimmer.stopShimmer();
                                imageShimmer.setVisibility(View.GONE);
                                image.setVisibility(View.VISIBLE);
                            }

                            //retry loading one more time
                            @Override
                            public void onError() {
                                Log.d("Debug", "IMAGE - Picasso Errored");
                                Snackbar.make(findViewById(R.id.detail_history_mega_layout), "Failed to load the capsule image",
                                        Snackbar.LENGTH_LONG)
                                        .setAction("Retry", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Picasso.with(DetailedCapsuleHistoryItem.this).load(imageLocation).fit().into(image);
                                            }
                                        })
                                        .show();
                            }
                        });
            }
        });
    }

    private void loadAvatar(){
        //avatar view has fix size, so no need to use viewtree
        Picasso.with(this)
                .load(avatarLocation)
                .fit()
                .into(avatar, new com.squareup.picasso.Callback() {

                    //once loaded, hide the placeholder shimmer, then show the avatar
                    @Override
                    public void onSuccess() {
                        avatarShimmer.stopShimmer();
                        avatarShimmer.setVisibility(View.GONE);
                        avatar.setVisibility(View.VISIBLE);
                    }

                    //retry loading one more time
                    @Override
                    public void onError() {
                        Log.d("Debug", "AVATAR - Picasso Errored");
                        Snackbar.make(findViewById(R.id.detail_history_mega_layout), "Failed to load user avatar of the capsule owner",
                                Snackbar.LENGTH_LONG)
                                .setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Picasso.with(DetailedCapsuleHistoryItem.this).load(avatarLocation).fit().into(avatar);
                                    }
                                })
                                .show();
                    }
                });
    }

    //TODO: @CHENFU ---- testing purpose only, assume finish loading voice from server takes 3 seconds
    private void loadVoice(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                voiceShimmer.stopShimmer();
                voiceShimmer.setVisibility(View.GONE);
                voice.setVisibility(View.VISIBLE);
            }
        },3000);
    }
}