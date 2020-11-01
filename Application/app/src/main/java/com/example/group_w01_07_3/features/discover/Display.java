package com.example.group_w01_07_3.features.discover;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.history.DetailedCapsuleHistoryItem;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.RecordAudioUtil;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
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
    private Toolbar mToolbar;
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

    /**
     *initialize display activity,read the information of capsule need to display, received from
     * discover page
     * @param savedInstanceState the data it most recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mToolbar = findViewById(R.id.display_history_capsule_back_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Opened Capsule");

        //navigate back to account page.
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
        //when audio is loaded successfully, remove the shimmer effect and set audio button to be
        //visible
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                shimmerVoice.stopShimmer();
                shimmerVoice.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        });
        startPlay = true;
        //set listener to listener to user's click on audio play button
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
            Toast.makeText(getApplicationContext(), "There is problem on the opening capsule", Toast.LENGTH_LONG);
            Log.d(TAG, "onCreate: " + "There is problem on capsule Information");
        }
    }

    /**
     * Reads information from intent about content to display,display the information including
     * title, content, date of open the capsule, optional image, optional audio, avater of user,
     * name of user
     * @param capsuleInfo information need to display, store in an json object
     * @throws JSONException possible exception on information reading
     */
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

        if (!this.isDestroyed()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    date.setText( "Opened at: " + open_date);
                    title.setText(capsuleTitle);
                    content.setText(capsuleContent);
                    username.setText(name);
                    //check the privacy status of capsule
                    if(private_status==1){
                        privacy.setText("Public Memory Capsule");
                    }else{
                        privacy.setText("Your Private Memory Capsule");
                    }

                    if (audiolink != "null") {
                        loadVoice();
                    } else {
                        //stop shimmer effect, set audio button to be invisible
                        shimmerVoice.stopShimmer();
                        shimmerVoice.setVisibility(View.GONE);
                    }
                    //if there is image, load image to image view,otherwise, use place holder image
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
                    //if there is avader link, load it to corresponding place, otherwise, use place
                    //holder.
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
    }

    private void loadImage(){
        //Must use  this tree observer to load content image
        img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure we call this only once
                img.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                //use Glide to load image, once successful loaded, turn off shimmer and display image
                Glide.with(Display.this)
                        .load(imagelink)
                        .apply(new RequestOptions().override(img.getWidth(),0))
                        .listener(new RequestListener<Drawable>() {
                            /**
                             * If the image could not be loaded, display the internet connection error
                             * to user
                             * @param e exception containing information about why the request failed
                             * @param model model we were trying to load when the exception occurred
                             * @param target  target we were trying to load the image into
                             * @param isFirstResource true if this exception is for the first resource to load.
                             * @return
                             */
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d("Debug", "IMAGE - Glide Errored");
                                Snackbar.make(findViewById(R.id.display_history_mega_layout),
                                        "Failed to load the capsule image, please check your internet connection",
                                        Snackbar.LENGTH_LONG)
                                        .show();
                                return false;
                            }

                            /**
                             * Remove shimmer effect if the image is loaded successfully.
                             * @param resource resource that was loaded for the target.
                             * @param model specific model that was used to load the image.
                             * @param target target the model was loaded into.
                             * @param dataSource the resource was loaded from.
                             * @param isFirstResource true if this is the first resource to in this load to be loaded into the target
                             * @return
                             */
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                shimmerImage.stopShimmer();
                                shimmerImage.setVisibility(View.GONE);
                                img.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(img);
            }
        });
    }

    /**
     *  load avater photo to image view to display, handle failure condition,remove shrimmer effect
     *  if load successfully.
     */
    private void loadAvatar(){
        //avatar view has fix size, so no need to use viewtree
        Glide.with(this)
                .load(avater_link)
                .listener(new RequestListener<Drawable>() {
                    /**
                     * Display failure information of loading avatar
                     * @param e exception containing information about why the request failed
                     * @param model model we were trying to load when the exception occurred
                     * @param target  target we were trying to load the image into
                     * @param isFirstResource true if this exception is for the first resource to load.
                     * @return
                     */
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d("Debug", "IMAGE - Glide Errored");
                        Snackbar.make(findViewById(R.id.display_history_mega_layout),
                                "Failed to load user avatar of the capsule owner, please check your internet connection",
                                Snackbar.LENGTH_LONG)
                                .show();
                        return false;
                    }

                    /**
                     * Remove shimmer effect if the avatar is loaded successfully.
                     * @param resource resource that was loaded for the target.
                     * @param model specific model that was used to load the image.
                     * @param target target the model was loaded into.
                     * @param dataSource the resource was loaded from.
                     * @param isFirstResource true if this is the first resource to in this load to be loaded into the target
                     * @return
                     */
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        shimmerAvatar.stopShimmer();
                        shimmerAvatar.setVisibility(View.GONE);
                        profile.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(profile);
    }

    /**
     * load audio to display, the audio could be replayed automatically, handle the situation if
     * audio could not be loaded due to internet failure.
     */
    private void loadVoice(){
        try {
            mediaPlayer.setDataSource(audiolink);
            mediaPlayer.prepareAsync();
            mediaPlayer.setLooping(true);
            //handle the internet loss condition, notify the user about Internet connection failure.
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                //handle media player lose network connection
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    if (i==MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                        Snackbar.make(findViewById(R.id.display_history_mega_layout),
                                "Failed to Load audio, please check your internet connection",
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                    return false;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop playing the music and release the resource
     */
    private void releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Back to the discover capsule page.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay,R.anim.pop_in);
        //stop the audio play, if the user click back button
        releaseMediaPlayer();
    }

    //stop music if click home button
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
    //stop the music if current page is pause 
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }
}

