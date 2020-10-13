package com.example.group_w01_07_3.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;

import java.io.IOException;

public class RecordAudioUtil {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private final int REQUEST_PERMISSION_AUDIO = 3;
    private AppCompatActivity context;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    public RecordAudioUtil(AppCompatActivity activity){

        context = activity;
        fileName = context.getApplicationContext().getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

    }


    public void onRecord(boolean start) {

        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    public void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }
    public boolean checkPermission(){
        int recordPermit = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO);
        if (recordPermit != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{
                    Manifest.permission.RECORD_AUDIO},REQUEST_PERMISSION_AUDIO);
        }

        if(ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
    public void onStop(){
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }


}
    