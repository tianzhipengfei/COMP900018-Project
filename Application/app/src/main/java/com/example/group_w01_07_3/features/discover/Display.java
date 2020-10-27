package com.example.group_w01_07_3.features.discover;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.history.DetailedCapsuleHistoryItem;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.RecordAudioUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class Display extends AppCompatActivity {
    private static final String TAG = "Display Activity";
    private JSONObject capsuleInfo;
    private ImageView img;
    private RecordAudioUtil media;
    private TextView title;
    private TextView content;
    private ImageButton play;
    //private Button stop;
    private int private_status;
    private MediaPlayer mediaPlayer;
    private String capsuleTitle;
    private String capsuleContent;
    private String imagelink;
    private String audiolink;
    private Boolean startPlay;
    private TextView username;
    private ImageView profile;
    private String name;
    private String avater_link;
    private String open_date;
    private TextView date;
    private TextView privacy;

    //Shimmer Place hodler Section
    private ShimmerFrameLayout shimmerImage, shimmerAvatar, shimmerVoice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Bundle extra_information = getIntent().getExtras();
        String extra = getIntent().getStringExtra("capsule");
        Log.d("The intent information", "onCreate: " + extra);
        privacy=(TextView) findViewById(R.id.display_detail_capsule_private_public_tag);
        img = (ImageView) findViewById(R.id.display_detail_image);
        title = (TextView) findViewById(R.id.display_detail_title);
        content = (TextView) findViewById(R.id.display_detail_content);
        play = (ImageButton) findViewById(R.id.display_audio_play);
        username=(TextView) findViewById(R.id.display_detail_username);
        profile=(ImageView) findViewById(R.id.display_detail_capsule_original_user_avatar);
        date=(TextView) findViewById(R.id.display_detail_date);

        shimmerImage = findViewById(R.id.display_detail_shimmer_image);
        shimmerAvatar = findViewById(R.id.display_detail_shimmer_avatar);
        shimmerVoice = findViewById(R.id.display_detail_shimmer_voice);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                shimmerVoice.stopShimmer();
                shimmerVoice.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        });

        startPlay = true;
        play.setOnClickListener(new View.OnClickListener() {
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

        if (extra != null) {
            try {
                JSONObject capsuleInfo = new JSONObject(extra);
                display(capsuleInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "There is problem on openning capsule", Toast.LENGTH_SHORT);
            Log.d(TAG, "onCreate: " + "There is problem on capsule Information");
        }
    }

    private void display(JSONObject capsuleInfo) throws JSONException {
        private_status=capsuleInfo.getInt("cpermission");
        capsuleTitle = capsuleInfo.getString("ctitle");
        capsuleContent = capsuleInfo.getString("ccontent");
        open_date=Calendar.getInstance().getTime().toString();
        imagelink = capsuleInfo.getString("cimage");
        Log.d(TAG, "display: " + "the new image Link" + imagelink);
        audiolink = capsuleInfo.getString("caudio");
        name=capsuleInfo.getString("cusr");
        avater_link = capsuleInfo.getString("cavatar");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                date.setText(open_date);
                title.setText(capsuleTitle);
                content.setText(capsuleContent);
                username.setText(name);

                if(private_status==1){
                    privacy.setText("public capsule");
                }else{
                    privacy.setText("private capsule");
                }

                if (audiolink != "null") {
                    loadVoice();
                } else {
                    shimmerVoice.stopShimmer();
                    shimmerVoice.setVisibility(View.GONE);
//                    play.setVisibility(View.VISIBLE);
                }

                if (imagelink != "null"){
                    loadImage();
                } else {
                    shimmerImage.stopShimmer();
                    shimmerImage.setVisibility(View.GONE);
                    img.requestLayout();
                    img.setMinimumHeight(48);
                    img.setVisibility(View.VISIBLE);

//                    img.setImageResource(R.drawable.gradient_1);
                }

                if (avater_link!= "null" ){
                    loadAvatar();
                } else {
                    shimmerAvatar.stopShimmer();
                    shimmerAvatar.setVisibility(View.GONE);
                    profile.setVisibility(View.VISIBLE);
                    profile.setImageResource(R.drawable.avatar_sample);
                }

            }
        });
    }

    private void loadImage(){
        //Must use  this tree observer to load content image
        img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure we call this only once
                img.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Picasso.with(Display.this)
                        .load(imagelink)
                        .resize(img.getWidth(),0)
                        .into(img, new com.squareup.picasso.Callback() {

                            //once loaded, hide the placeholder shimmer, then show the user image
                            @Override
                            public void onSuccess() {
                                shimmerImage.stopShimmer();
                                shimmerImage.setVisibility(View.GONE);
                                img.setVisibility(View.VISIBLE);
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
                                                Picasso.with(Display.this).load(imagelink).fit().into(img);
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
                .load(avater_link)
                .fit()
                .into(profile, new com.squareup.picasso.Callback() {

                    //once loaded, hide the placeholder shimmer, then show the avatar
                    @Override
                    public void onSuccess() {
                        shimmerAvatar.stopShimmer();
                        shimmerAvatar.setVisibility(View.GONE);
                        profile.setVisibility(View.VISIBLE);
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
                                        Picasso.with(Display.this).load(avater_link).fit().into(profile);
                                    }
                                })
                                .show();
                    }
                });
    }

    private void loadVoice(){
        try {
//            play.setVisibility(View.VISIBLE);
            mediaPlayer.setDataSource(audiolink);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

