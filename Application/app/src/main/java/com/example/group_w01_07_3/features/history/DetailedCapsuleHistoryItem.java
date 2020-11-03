package com.example.group_w01_07_3.features.history;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.util.MessageUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

public class DetailedCapsuleHistoryItem extends AppCompatActivity {
    //APP view
    private CoordinatorLayout coordinatorLayout;
    private Toolbar mToolbar;
    private TextView title;
    private TextView date;
    private TextView tag;
    private TextView content;
    private TextView username;
    private ImageView image;
    private ImageView avatar;
    private ImageButton voice;
    private ShimmerFrameLayout imageShimmer, avatarShimmer, voiceShimmer;

    //Media Player Section
    private MediaPlayer mediaPlayer;
    private Boolean startPlay;

    //Capsule Content Section
    OpenedCapsule item;
    String titleString, dateString, usernameString, contentString;
    int tagIndentifier;
    String imageLocation, avatarLocation, voiceLocation;

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

        initView();

        //get the capsule object
        item = (OpenedCapsule) getIntent().getExtras().getSerializable("capsuleObject");

        loadCapsule(item);
    }

    /**
     * Initialize all view required to show the opened Geo-capsule
     */
    private void initView() {
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

        coordinatorLayout = findViewById(R.id.detail_history_mega_layout);
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
    }

    /**
     * Load all information of the capsule from server or directly from the object.
     *
     * @param item The capsule object to be displayed
     */
    private void loadCapsule(OpenedCapsule item) {
        titleString = item.getCapsule_title();
        dateString = item.getOpened_date();
        tagIndentifier = item.getTag();
        usernameString = item.getUsername();
        contentString = item.getContent();

        imageLocation = item.getCapsule_url();
        avatarLocation = item.getAvatar_url();
        voiceLocation = item.getVoice_url();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title.setText(titleString);
                date.setText(dateString);
                content.setText(contentString);
                username.setText(usernameString);

                if (tagIndentifier == 1) {
                    tag.setText("Public Geo-Capsule");
                } else {
                    tag.setText("Your Private Geo-Capsule");
                }

                if (!imageLocation.equals("null")) {
                    loadImage();
                } else {
                    imageShimmer.stopShimmer();
                    imageShimmer.setVisibility(View.GONE);
                    android.view.ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                    layoutParams.height = 48;
                    image.setVisibility(View.VISIBLE);
                }

                if (!avatarLocation.equals("null")) {
                    loadAvatar();
                } else {
                    avatarShimmer.stopShimmer();
                    avatarShimmer.setVisibility(View.GONE);
                    avatar.setVisibility(View.VISIBLE);
                    avatar.setImageResource(R.drawable.avatar_sample);
                }

                if (!voiceLocation.equals("null")) {
                    loadVoice();
                } else {
                    voiceShimmer.stopShimmer();
                    voiceShimmer.setVisibility(View.GONE);
                }
            }
        });

    }

    /**
     * load capsule content image from the 3rd party image server using Glide.
     */
    private void loadImage() {
        //Must use  this tree observer to load content image
        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure we call this only once
                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                //ONLY load if the activity is still alive
                if (!DetailedCapsuleHistoryItem.this.isDestroyed()) {
                    //use Glide to load image, once successful loaded, turn off shimmer and display image
                    Glide.with(DetailedCapsuleHistoryItem.this)
                            .load(imageLocation)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    MessageUtil.displaySnackbar(coordinatorLayout,
                                            "Failed to load the capsule image, please check your internet connection",
                                            Snackbar.LENGTH_LONG);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imageShimmer.stopShimmer();
                                    imageShimmer.setVisibility(View.GONE);
                                    image.setVisibility(View.VISIBLE);
                                    return false;
                                }
                            })
                            .into(image);
                } else {
                    Log.d("FINISHED", "run: Activity has been finished, don't load Glide for image");
                }

            }
        });
    }

    /**
     * load capsule creator avatar from the 3rd party image server using Glide.
     */
    private void loadAvatar() {
        //avatar view has fix size, so no need to use viewtree
        //use Glide to load avatar, once successful loaded, turn off shimmer and display avatar

        //ONLY load if the activity is still alive
        if (!DetailedCapsuleHistoryItem.this.isDestroyed()) {
            Glide.with(this)
                    .load(avatarLocation)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            MessageUtil.displaySnackbar(coordinatorLayout,
                                    "Failed to load user avatar of the capsule owner, please check your internet connection",
                                    Snackbar.LENGTH_LONG);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            avatarShimmer.stopShimmer();
                            avatarShimmer.setVisibility(View.GONE);
                            avatar.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(avatar);
        } else {
            Log.d("FINISHED", "run: Activity has been finished, don't load Glide for avatar");
        }
    }

    /**
     * load voice data of the capsule from server
     */
    private void loadVoice() {
        //ONLY load if the activity is still alive
        if (!DetailedCapsuleHistoryItem.this.isDestroyed()) {
            mediaPlayer = new MediaPlayer();
            //when audio is loaded successfully, remove the shimmer effect and set audio button to be visible
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    voiceShimmer.stopShimmer();
                    voiceShimmer.setVisibility(View.GONE);
                    voice.setVisibility(View.VISIBLE);
                }
            });
            startPlay = true;

            //set listener to listener to user's click on audio play button
            voice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startPlay) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.pause();
                    }
                    startPlay = !startPlay;
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    startPlay = !startPlay;
                }
            });

            try {
                mediaPlayer.setDataSource(voiceLocation);
                mediaPlayer.prepareAsync();
                mediaPlayer.setLooping(false);
                //handle the internet loss condition, notify the user about Internet connection failure.
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    //handle media player lose network connection
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        MessageUtil.displaySnackbar(coordinatorLayout,
                                "Failed to Load audio, please check your internet connection",
                                Snackbar.LENGTH_LONG);
                        return false;
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Back to the discover capsule page.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.pop_in);
        //stop the audio play, if the user
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * stop music if click home button
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * stop the music if current page is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
}