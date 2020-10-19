package com.example.group_w01_07_3.features.create;

import android.Manifest;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.LocationUtil;
import com.example.group_w01_07_3.util.RecordAudioUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

public class CreateCapsule extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private boolean mStartRecording = true;
    boolean mStartPlaying = true;

    private DrawerLayout drawerLayout;
    View headerview;
    TextView headerUsername;
    private String usernameProfileString;

    private RecordAudioUtil recorderUtil;
    private LocationUtil locationUtil ;
    private int permission = 1;


    JSONObject capsuleInfo = new JSONObject();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);
        recorderUtil= new RecordAudioUtil(this);
        locationUtil= new LocationUtil(this);
        //don't pop up keyboard automatically when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        //设置主Activity的toolbar, 以及初始化侧滑菜单栏
        Toolbar toolbar = findViewById(R.id.toolbar_create);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Create Memory Capsule");

        drawerLayout = findViewById(R.id.create_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //设置侧滑菜单栏
        NavigationView navigationView = findViewById(R.id.nav_view_create);
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        //the logic to find the header, then update the username from server user profile
        headerview = navigationView.getHeaderView(0);
        headerUsername = headerview.findViewById(R.id.header_username);

        updateHeaderUsername();
    }

    public void whetherPublic(View v) {
        Switch permiSwitch = (Switch) findViewById(R.id.create_capsule_permission);
        if (permiSwitch.isChecked()) {
            permission = 0;
        } else {
            permission = 1;
        }

        if (permission == 1) {
            permiSwitch.setText("create public capsule");
        } else {
            permiSwitch.setText("create private capsule");
        }
    }


    private boolean getLocation() throws JSONException {

        Location location = locationUtil.getLocation();
        if (location != null) {
            try {
                capsuleInfo.put("lat", location.getLatitude());
                capsuleInfo.put("lat", location.getLongitude());
                return true;
            } catch (JSONException e) {
                System.out.print("Problems happen during parsing json objects");
                return false;
            }
        } else {
            capsuleInfo.put("lat", 37.4219983);
            capsuleInfo.put("lon", -122.084);
            return true;
        }

    }

    private boolean getOtherInfo() throws JSONException {
        EditText capsuleTitle = findViewById(R.id.create_capsule_title);
        EditText capsuleContent = findViewById(R.id.create_capsule_content);
        if (capsuleTitle.getText().toString().isEmpty()) {
            Toast.makeText(CreateCapsule.this, "Please enter the title",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (capsuleContent.getText().toString().isEmpty()) {
            Toast.makeText(CreateCapsule.this, "Please enter the content",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            capsuleInfo.put("title", capsuleTitle.getText());
            capsuleInfo.put("content", capsuleContent.getText());
            capsuleInfo.put("time", Calendar.getInstance().getTime());
            capsuleInfo.put("permission", permission);
            //for testing
            capsuleInfo.put("tkn", "59c43e5670cfd24da97c607a5759aa33d88fdbc5");
        } catch (JSONException e) {
            System.out.print("Problems happen during parsing json objects");
        }
        return true;
    }

    public void recordAudio(View v) {
        // check permission
        if (recorderUtil.checkPermission()) {
            Button recordButton = findViewById(R.id.record_button);
            recorderUtil.onRecord(mStartRecording);
            if (mStartRecording) {
                recordButton.setText("STOP");
            } else {
                recordButton.setText("RECORD");
            }
            mStartRecording = !mStartRecording;
        } else {
            Toast.makeText(getApplicationContext(), "Need permission", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void playAudio(View v) {
        Button playButton = findViewById(R.id.play_button);
        recorderUtil.onPlay(mStartPlaying);
        if (mStartPlaying) {
            playButton.setText("STOP");
        } else {
            playButton.setText("PLAY");
        }
        mStartPlaying = !mStartPlaying;


    }



    public void createCapsule(View v) throws JSONException {
        if (getLocation() && getOtherInfo()) {
            //collect info
            ;
            Log.d("CapsuleInfo", capsuleInfo.toString());
            HttpUtil.createCapsule(capsuleInfo, new okhttp3.Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(CreateCapsule.this, "connection fail", Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseData = response.body().string();
                    Log.d("CREATECAPSULE", responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.d("CREATECAPSULE", status);
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  Toast.makeText(
                                                          CreateCapsule.this,
                                                          "Create Capsule successfully",
                                                          Toast.LENGTH_SHORT).show();
//
                                              }
                                          }
                            );
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public void cancel(View v) {
        startActivity(new Intent(CreateCapsule.this, DiscoverCapsule.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.discover_capsule_tab:
                intent = new Intent(CreateCapsule.this, DiscoverCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.create_capsule_tab:
                //main activity cannot start itself again
                return true;
            case R.id.capsule_history_tab:
                intent = new Intent(CreateCapsule.this, OpenedCapsuleHistory.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.edit_profile_tab:
                intent = new Intent(CreateCapsule.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
        }


        return false;
    }

    private void updateHeaderUsername(){
        if(!UserUtil.getToken(CreateCapsule.this).isEmpty()){
            HttpUtil.getProfile(UserUtil.getToken(CreateCapsule.this), new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d("PROFILE", "***** getProfile onResponse *****");
                    String responseData = response.body().string();
                    Log.d("PROFILE", "getProfile: " + responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.d("PROFILE", "getProfile success: " + status);
                            String userInfo = responseJSON.getString("userInfo");
                            JSONObject userInfoJSON = new JSONObject(userInfo);
                            usernameProfileString = userInfoJSON.getString("uusr");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    headerUsername.setText(usernameProfileString);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void onStop() {

        super.onStop();
        recorderUtil.onStop();
    }
}
