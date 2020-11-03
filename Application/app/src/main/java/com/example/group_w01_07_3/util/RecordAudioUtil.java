package com.example.group_w01_07_3.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;

/**
 * RecordAudioUtil is used to record the audio and play the audio
 */
public class RecordAudioUtil {

    // for record audio
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private final int REQUEST_PERMISSION_AUDIO = 3;
    private AppCompatActivity context;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private boolean stream = false;

    // requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    /**
     * constructor
     *
     * @param activity activity that needs record audio
     */
    public RecordAudioUtil(AppCompatActivity activity) {
        context = activity;
        fileName = context.getApplicationContext().getExternalCacheDir().getAbsolutePath();
        fileName += "/capsuleRecording.aac";
    }

    /**
     * constructor
     *
     * @param filePath filePath of the file
     */
    public RecordAudioUtil(String filePath) {
        fileName = filePath;
        stream = true;
    }

    /**
     * record audio
     *
     * @param start whether to start or stop
     */
    public void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * play audio
     *
     * @param start whether start or stop
     */
    public void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }


    /**
     * check the permission to record audio
     *
     * @return is permitted or not
     */
    public boolean checkPermission() {
        int recordPermit = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO);
        if (recordPermit != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{
                    Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_AUDIO);
        }

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * start playing the audio
     */
    private void startPlaying() {
        if (getAudioFile().exists()) {
            player = new MediaPlayer();
            try {
                player.setDataSource(fileName);
                if (stream == true) {
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                player.prepare();
                player.start();

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer m) {
                        MaterialButton playButton = context.findViewById(R.id.play_button);
                        playButton.setIconResource(R.drawable.voice_play);
                        playButton.setText("PLAY");
                        ((CreateCapsule) context).setmStartPlaying(!((CreateCapsule) context).getmStartPlaying());
                    }
                });
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        } else {
            MaterialButton playButton = context.findViewById(R.id.play_button);
            playButton.setIconResource(R.drawable.voice_play);
            playButton.setText("PLAY");
            MessageUtil.displayToast(this.context, "You haven't recorded the audio", Toast.LENGTH_SHORT);
            ((CreateCapsule) context).setmStartPlaying(!((CreateCapsule) context).getmStartPlaying());
        }
    }

    /**
     * stop playing the audio
     */
    private void stopPlaying() {
        player.release();
        player = null;
    }

    /**
     * start recoding the audio
     */
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }

    /**
     * stop recording the audio
     */
    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    /**
     * stop the recorder and the the player
     */
    public void onStop() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    /**
     * get the recorded audio file
     *
     * @return the recorded audio file
     */
    public File getAudioFile() {
        return new File(fileName);
    }

}