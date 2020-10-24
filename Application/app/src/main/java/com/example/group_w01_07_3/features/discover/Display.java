package com.example.group_w01_07_3.features.discover;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.RecordAudioUtil;
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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Bundle extra_information = getIntent().getExtras();
        String extra = getIntent().getStringExtra("capsule");
        Log.d("The intent information", "onCreate: " + extra);
        img = (ImageView) findViewById(R.id.display_detail_image);
        title = (TextView) findViewById(R.id.display_detail_title);
        content = (TextView) findViewById(R.id.display_detail_content);
        play = (ImageButton) findViewById(R.id.display_audio_play);
        username=(TextView) findViewById(R.id.display_detail_username);
        profile=(ImageView) findViewById(R.id.display_detail_capsule_original_user_avatar);
        date=(TextView) findViewById(R.id.display_detail_date);
        mediaPlayer = new MediaPlayer();
        startPlay = true;
        //stop=(Button) findViewById(R.id.stop_button);
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
                if (audiolink != "null") {
                    try {
                        mediaPlayer.setDataSource(audiolink);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    media=new RecordAudioUtil(audiolink);
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (startPlay) {
                                mediaPlayer.start();
                            } else {
                                mediaPlayer.stop();
                            }
                            startPlay = !startPlay;
                        }
                    });
                }
                if (imagelink != "null") {
                    img.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext()).load(imagelink).into(img);
                }
                date.setText(open_date);
                title.setText(capsuleTitle);
                content.setText(capsuleContent);
                username.setText(name);
                Log.d(TAG, "run: "+avater_link);
                if(avater_link!=null){
                    profile.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext()).load(avater_link).into(profile);
                    Log.d(TAG, "run: "+"Has load the file");
                }
            }
        });
    }
}
//    public void audioPlay(View view) throws IOException {
//        if(startPlay){
//            mediaPlayer.start();
//        }else{
//            mediaPlayer.stop();
//        }
//        startPlay=!startPlay;
//        play=findViewById(R.id.play_button);
//        media.onPlay(startPlay);
//        if(startPlay){
//            play.setText("Stop");
//        }else{
//            play.setText("Play");
//        }
//        startPlay=!startPlay;
//    }

