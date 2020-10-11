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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

public class CreateCapsule extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener{

    private DrawerLayout drawerLayout;

    private int permission = 1;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private final int REQUEST_PERMISSION_COARSE_LOCATION = 2;
    JSONObject capsuleInfo = new JSONObject();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);

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

    private boolean checkPermission() {
        int fineLocation = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
        int corseLocation = ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (corseLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private boolean getLocation() throws JSONException {
        if (checkPermission()) {

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
                capsuleInfo.put("lat", -29.228890);
                capsuleInfo.put("lon", 141.544555);
                return true;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Need permission", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
    }

    private boolean getOtherInfo() throws JSONException {
        EditText capsuleTitle = findViewById(R.id.create_capsule_title);
        EditText capsuleContent = findViewById(R.id.create_capsule_content);
        if(capsuleTitle.getText().toString().isEmpty()){
            Toast.makeText(CreateCapsule.this, "Please enter the title",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if(capsuleContent.getText().toString().isEmpty()){
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

    public void createCapsule(View v) throws JSONException {
        if (getLocation()&&getOtherInfo()) {
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
//                                                  startActivity(new Intent(getApplicationContext(),
//                                                          HomeActivity.class));
//                                                  overridePendingTransition(android.R.anim.fade_in,
//                                                          android.R.anim.fade_out);
                                              }
                                          }
                            );
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            /*

            Toast.makeText(getApplicationContext(), capsuleInfo.toString(), Toast.LENGTH_SHORT)
                    .show();

            // Prepare for connect
            URL url = new URL("https://www.tianzhipengfei.xin/mobile/createCapsule");
            byte[] out = capsuleInfo.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);
            con.setFixedLengthStreamingMode(length);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            // response code is OK
            int responceCode = con.getResponseCode();

            if (responceCode == HttpURLConnection.HTTP_OK) {
                try (OutputStream os = con.getOutputStream()) {
                    os.write(out);
                    os.flush();

                } catch (Exception e) {
                    Log.d("Exception","Problems happen during transmission");

                }

                // Receive reply
                InputStream inputStream = con.getInputStream();
                BufferedReader responseReader = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8"));

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = responseReader.readLine()) != null) {
                    response.append(inputLine);
                }
                responseReader.close();
                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT)
                        .show();

            }else{
                Toast.makeText(getApplicationContext(), "connection fail", Toast.LENGTH_SHORT)
                        .show();
            }*/

        }
    }

    public void cancel(View v){
        startActivity(new Intent(CreateCapsule.this,DiscoverCapsule.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;
        //note. This is top level navigation(unrelated major task). Must use fade through
        switch (id){
            case R.id.discover_capsule_tab:
                intent = new Intent(CreateCapsule.this, DiscoverCapsule.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.create_capsule_tab:
                //main activity cannot start itself again
                return true;
            case R.id.capsule_history_tab:
                intent = new Intent(CreateCapsule.this, OpenedCapsuleHistory.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.edit_profile_tab:
                intent = new Intent(CreateCapsule.this, EditProfile.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
        }


        return false;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
