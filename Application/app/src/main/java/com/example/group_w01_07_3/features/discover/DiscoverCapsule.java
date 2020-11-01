package com.example.group_w01_07_3.features.discover;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DiscoverCapsule extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, SensorListener {
    // app view
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View headerview;
    ShapeableImageView headerAvatar;
    private TextView headerUsername;
    private String usernameProfileString, avatarProfileString;
    // capsules
    private JSONObject capsuleInfo = new JSONObject();
    private JSONArray allCapsules;
    private JSONObject selectedCapsule;
    private Marker selectedMarker;
    private Marker mCurrLocationMarker;
    private Hashtable<Marker, Object> mCapsuleMarkers = new Hashtable<Marker, Object>();
    private boolean if_connected = false;
    private boolean can_i_shake = false;
    private boolean can_i_retrieve_http = true;
    private boolean can_i_fresh_markers = false;
    // location permission
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private boolean disable_camera = true;
    // shake event
    public SensorManager sensorMgr;
    private boolean discover_refresh = true;
    private boolean shakeOpen = false;
    private long lastUpdate_shaking = System.currentTimeMillis();
    private float last_x = 0;
    private float last_y = 0;
    private float last_z = 0;
    private float last_move_x = 0;
    private float last_move_y = 0;
    private float last_move_z = 0;
    private int open_shake_time = 0;

    private static final int MAX_PAUSE_BETWEEN_SHAKES = 200;  // unit: default 200 ms
    private static final int NUM_SHAKES = 5; //5,valid shake how many times to open capsule
    private static final float COSINE = (float) 0.5; //0.5,cosine,rotation angle,0.8, 45" --> PHONE IS MORE SENSITIVE THAN EMULATOR, SMALLER THE HARDER
    private static final float FORCE_THRESHOLD = (float) 10; //rotation force, this is rotation force threshold on open capsule. --> PHONE IS MORE SENSITIVE THAN EMULATOR, THE BIGGER THE HARDER
    // last location requested
    private Location mLastLocation;
    private double lastRequestLat = 360.0;
    private double lastRequestLon = 360.0;
    private long lastUpdate_time;
    private double curLat = 360.0;
    private double curLon = 360.0;
    // time interval for updating current location (unit: ms)
    private final int PER_SECOND = 1000;
    private int locationUpdateInterval = 5 * PER_SECOND;
    // distance capsules request interval (unit: degree)
    private double distanceThresholdToRequest = 5.55;
    // pop-up window
    private boolean popUpShake = false;
    private PopupWindow pw;
    boolean doubleBackToExitPressedOnce = false;
    //Message Section
    Toast toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_capsule);

        //use toolbar at top of screen across all activities
        Toolbar toolbar = findViewById(R.id.toolbar_discover);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discover Memory Capsule");

        drawerLayout = findViewById(R.id.discover_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //slide-out sidebar
        navigationView = findViewById(R.id.nav_view_discover);
        navigationView.getMenu().getItem(0).setChecked(true); //setChecked myself
        navigationView.setNavigationItemSelectedListener(this);

        //the logic to find the header, then update the username from server user profile
        headerview = navigationView.getHeaderView(0);
        headerUsername = headerview.findViewById(R.id.header_username);
        headerAvatar = headerview.findViewById(R.id.header_avatar);

        updateHeader();

        //get current Location in GoogleMap using FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        displayToast(DiscoverCapsule.this,
                "Let's look for capsules nearby! Shake to refresh capsules", Toast.LENGTH_SHORT);

        registerShakeSensor();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.discover_capsule_tab:
                //main activity cannot start itself again
                return true;
            case R.id.create_capsule_tab:
                intent = new Intent(DiscoverCapsule.this, CreateCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            case R.id.capsule_history_tab:
                intent = new Intent(DiscoverCapsule.this, OpenedCapsuleHistory.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            case R.id.edit_profile_tab:
                intent = new Intent(DiscoverCapsule.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
        }
        return false;
    }

    private void updateHeader() {
        if (!UserUtil.getToken(DiscoverCapsule.this).isEmpty()) {
            HttpUtil.getProfile(UserUtil.getToken(DiscoverCapsule.this), new okhttp3.Callback() {
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
                            avatarProfileString = userInfoJSON.getString("uavatar");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!DiscoverCapsule.this.isDestroyed()) {
                                        headerUsername.setText(usernameProfileString);
                                        if (!(avatarProfileString == "null")) {
                                            Picasso.with(DiscoverCapsule.this)
                                                    .load(avatarProfileString)
                                                    .fit()
                                                    .placeholder(R.drawable.logo)
                                                    .into(headerAvatar);
                                        }
                                    } else {
                                        Log.d("FINISHED", "run: Activity has been finished, don't load Glide for update header avatar & username");
                                    }
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
                    //retry to update every 3 seconds. handle the case that enter the activity
                    //with no internet at all(which okHTTP will not retry for you)
                    if (!DiscoverCapsule.this.isDestroyed()) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateHeader();
                            }
                        }, 3000);
                    }
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // request location permission
        requestLocation();
        mapLayout();
    }

    private void requestLocation() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationUpdateInterval);
        mLocationRequest.setFastestInterval(locationUpdateInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // location permission was granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                // request location permission when it is the first time users use the app
                checkLocationPermission();
            }
        } else {
            // location permission was granted
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    // the result of location permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // if request was cancelled, the result arrays are empty.
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // location permission was granted, and restart google map
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(DiscoverCapsule.this, DiscoverCapsule.class);
                        startActivity(intent);
                    }
                } else {
                    // permission was denied
                    displayToast(this, "permission denied", Toast.LENGTH_LONG);
                }
                return;
            }
        }
    }

    private void mapLayout() {
        // clickable markers
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (Marker m : mCapsuleMarkers.keySet()) {
                    if (marker.equals(m)) {
                        selectedCapsule = (JSONObject) mCapsuleMarkers.get(m);
                        selectedMarker = m;
                        Log.w("MARKERS-MATCH", "******* popup window *******");
                        PopUpWindowFunction();
                        return true;
                    }
                }
                return false;
            }
        });

        mGoogleMap.setPadding(0, 155, 10, 0);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    // get location updates every second
    final LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            // send http capsule request using users' current location
            updateCapsuleRequestLocation(locationResult);
            requestCapsuleInfo();

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                check_ifCanRefreshMarkers(location);
                redrawGoogleMap(location);
            }
        }
    };

    private void updateCapsuleRequestLocation(LocationResult locationResult) {
        List<Location> locationList = locationResult.getLocations();

        if (locationList.size() > 0) {
            //the last location in the list is the newest
            Location location = locationList.get(locationList.size() - 1);
            curLat = location.getLatitude();
            curLon = location.getLongitude();

            try {
                if (checkForRequest(location.getLatitude(), location.getLongitude())) {
                    can_i_shake = false;
                    can_i_retrieve_http = true;
                    can_i_fresh_markers = false;

                    lastRequestLat = location.getLatitude();
                    lastRequestLon = location.getLongitude();

                    //move map camera to current location
                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastUpdate_time) > PER_SECOND) {
                        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                LatLng latLng2 = new LatLng(lastRequestLat, lastRequestLon);
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 18));
                                lastUpdate_time = System.currentTimeMillis();
                            }
                        });
                    }
                    capsuleInfo.put("lat", lastRequestLat);
                    capsuleInfo.put("lon", lastRequestLon);
                } else {
                    // current location
                    capsuleInfo.put("lat", curLat);
                    capsuleInfo.put("lon", curLon);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkForRequest(double curLat, double curLon) {
        if (lastRequestLat == 360 && lastRequestLon == 360) {
            return true;
        } else if (getDistance(curLat, curLon, lastRequestLat, lastRequestLon) > distanceThresholdToRequest) {
            System.out.println(getDistance(curLat, curLon, lastRequestLat, lastRequestLon));
            return true;
        }
        return false;
    }

    // calculate distance by latitude, and longitude (unit: Kilometers)
    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 111.18957696;
            return dist;
        }
    }

    // retrieve capsule information through HTTP GET method
    private void requestCapsuleInfo() {

        // Only request capsule from server if discover is still alive
        // handled the case if switch between activity, then we will have a stack of capsule list to updated
        // no matter online or offline
        if (!this.isDestroyed()) {
            if (capsuleInfo.length() == 0) {
                displayToast(DiscoverCapsule.this, "No token to get capsule", Toast.LENGTH_SHORT);
                Log.d("CAPSULE", "***** No token to get capsule *****");
                allCapsules = new JSONArray();
                selectedCapsule = new JSONObject();
            } else if (can_i_shake == false && can_i_retrieve_http == true && can_i_fresh_markers == false && discover_refresh == true) {
                try {
                    String token = UserUtil.getToken(DiscoverCapsule.this);
                    Log.i("SENDING-REQUEST", "capsuleInfo:" + capsuleInfo);
                    HttpUtil.getCapsule(token, capsuleInfo, new Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            Log.d("RECEIVED-CAPSULE", "***** getCapsule onResponse *****");
                            String responseData = response.body().string();
                            Log.i("RECEIVED-CAPSULE", "responseData:" + responseData);
                            try {
                                JSONObject responseJSON = new JSONObject(responseData);
                                if (responseJSON.has("success")) {
                                    allCapsules = responseJSON.getJSONArray("capsules");
                                    // refresh capsules only after receiving http response
                                    can_i_shake = false;
                                    can_i_retrieve_http = false;
                                    can_i_fresh_markers = true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            e.printStackTrace();
                            Snackbar snackbar = Snackbar
                                    .make(drawerLayout, "Oops. Looks like you lost Internet connection\nReconnecting...", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("FINISHED", "requestCapsuleInfo: Don't send HTTP request if finishing Discover.class");
            }
        }

    }

    private void check_ifCanRefreshMarkers(Location location) {

        //Same reason, don't drawer marker if discover activity has been finished
        if (!this.isDestroyed()) {
            if (can_i_shake == false && can_i_retrieve_http == false && can_i_fresh_markers == true && discover_refresh == true) {
                refreshMarkers(allCapsules);

                //ensure there is a 3 seconds gap between [next shake event] & [current completed marker fresh]
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        can_i_shake = true;
                        can_i_retrieve_http = false;
                        can_i_fresh_markers = false;
                    }
                }, 2000);
            }
            if (mCapsuleMarkers.isEmpty()) {
                can_i_shake = false;
                can_i_retrieve_http = true;
                can_i_fresh_markers = false;
            }
        }
    }

    public void refreshMarkers(JSONArray allCapsules) {
        mGoogleMap.clear();
        Log.d("CAPSULEMARKER", "allCapsules: " + allCapsules);

        // for testing
        int counts = 0;

        //place capsule markers on google map
        for (int i = 0; i < allCapsules.length(); i++) {
            try {
                JSONObject objects = allCapsules.getJSONObject(i);
                Double lat = objects.getDouble("clat");
                Double lng = objects.getDouble("clon");
                Integer permission = objects.getInt("cpermission");
                Log.d("CAPSULEMARKER", "cpermission:" + permission);

                LatLng lat_Lng = new LatLng(lat, lng);
                MarkerOptions capsuleMarker = new MarkerOptions();
                capsuleMarker.position(lat_Lng);
                capsuleMarker.title("Capsule");
                capsuleMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.capsule_marker));

                // add capsule markers
                Marker tmp = mGoogleMap.addMarker(capsuleMarker);
                mCapsuleMarkers.put(tmp, allCapsules.get(i));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // for testing
            counts += 1;
            Log.d("CAPSULEMARKER", "refresh_counts: " + counts);
        }
        displayToast(DiscoverCapsule.this, "Refresh successfully!", Toast.LENGTH_SHORT);

        registerShakeSensor();
    }

    // redraw google map after users refresh capsules
    private void redrawGoogleMap(Location location) {
        // default location Googleplex: 37.4219983 -122.084
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mCurrLocationMarker.showInfoWindow();
    }
    //The logic is borrowed from https://stackoverflow.com/questions/44992014/how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient

    //double backpressed to exit app
    //The logic is borrowed from https://stackoverflow.com/questions/8430805/clicking-the-back-button-twice-to-exit-an-activit
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            displayToast(this, "Press back again to exit", Toast.LENGTH_SHORT);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void registerShakeSensor() {
        // handle the case of finish current activity but the delayed response register shake
        // again in another activity. So need to check if my self has been destroyed or not
        if (this.isDestroyed()) {

        } else {
            //set up sensor manager
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorMgr.registerListener(this,
                    SensorManager.SENSOR_ACCELEROMETER,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    // detect a shake event
    @Override
    public void onSensorChanged(int sensor, float[] values) {
        //when detect 10 times of slight shake, open the capsule
        if (open_shake_time == NUM_SHAKES) {
            sensorMgr.unregisterListener(this);

            open_shake_time = 0;
            shakeOpen = true;
            if (popUpShake) {
                RequestSending();
            } else {
                // shake to refresh capsules
                can_i_shake = false;
                can_i_retrieve_http = true;
                can_i_fresh_markers = false;
            }
        }

        // increment of shake success event. valid for three conditions
        // 1: no pop window, 2: discover in request info state, but pop window(shake) opened
        // 3: discover in refresh map state, , but pop window(shake) opened
        if (sensor == SensorManager.SENSOR_ACCELEROMETER && can_i_shake == true
                && can_i_retrieve_http == false && can_i_fresh_markers == false) {
            addShakeSuccessCount(values);
        } else if (sensor == SensorManager.SENSOR_ACCELEROMETER && can_i_shake == false
                && can_i_retrieve_http == true && can_i_fresh_markers == false
                && discover_refresh == false && popUpShake == true) {
            addShakeSuccessCount(values);
        } else if (sensor == SensorManager.SENSOR_ACCELEROMETER && can_i_shake == false
                && can_i_retrieve_http == false && can_i_fresh_markers == true
                && discover_refresh == false && popUpShake == true) {
            addShakeSuccessCount(values);
        }
    }
    //The logic is borrowed from https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android

    private void addShakeSuccessCount (float[] values){
        float x = values[SensorManager.DATA_X];
        float y = values[SensorManager.DATA_Y];
        float z = values[SensorManager.DATA_Z];
        if (last_x == 0 && last_y == 0 && last_z == 0) {
            last_x = x;
            last_y = y;
            last_z = z;
            return;
        }
        float cur_move_x = x - last_x;
        float cur_move_y = y - last_y;
        float cur_move_z = z - last_z;
        long curTime = System.currentTimeMillis();
        // check if the last movement was not long ago
        if ((curTime - lastUpdate_shaking) > MAX_PAUSE_BETWEEN_SHAKES) {
            lastUpdate_shaking = curTime;
            //detect the reasonable shake of capsule
            float cur_move_length = (float) Math.sqrt(cur_move_x * cur_move_x + cur_move_y * cur_move_y + cur_move_z * cur_move_z);
            float last_move_length = (float) Math.sqrt(last_move_x * last_move_x + last_move_y * last_move_y + last_move_z * last_move_z);
            if (cur_move_length > FORCE_THRESHOLD) {
                float product = cur_move_x * last_move_x + cur_move_y * last_move_y + cur_move_z * last_move_z;
                float length = cur_move_length * last_move_length;
                if (product / length < COSINE) {
                    if ((popUpShake && !shakeOpen) || (discover_refresh)) {
                        open_shake_time += 1;
                        Log.d("SHAKE", "onSensorChanged: shake success + 1");
                    }
                }
            }
            last_move_x = cur_move_x;
            last_move_y = cur_move_y;
            last_move_z = cur_move_z;
        }
    }

    @Override
    public void onAccuracyChanged(int i, int i1) {

    }

    public void PopUpWindowFunction() {
        //first thing to do is disable shake function for discover activity when window is poped
        discover_refresh = false;

        Log.w("MARKERS-MATCH", "******* FIRE POP WINDOW*******");

        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        int width = (getWindowManager().getDefaultDisplay().getWidth() * 3) / 4;

        if (pw != null && pw.isShowing()) {
            Log.d("popwindow", "PopUpWindowFunction: one window already showing, don't pop another");
        } else {
            Random choice = new Random();
            int selection = choice.nextInt(3);
            switch (selection) {
                case 0:
                    final View popupview_tap = in.inflate(R.layout.popup_tap, null);
                    pw = new PopupWindow(popupview_tap, width, height, true);
                    pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            Log.d("POPWINDOW", "onDismiss: ");

                            //as popshake is already closed, so just need to reopen discover shake
                            discover_refresh = true;
                        }
                    });
                    pw.setOutsideTouchable(false);
                    pw.setAnimationStyle(R.style.popup_window_animation);
                    pw.showAtLocation(popupview_tap, Gravity.CENTER, 0, 0);
                    ImageView button = (ImageView) popupview_tap.findViewById(R.id.dismiss);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pw.dismiss();
                        }
                    });
                    View img = popupview_tap.findViewById(R.id.tap_me);
                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RequestSending();
                        }
                    });
                    break;
                case 1:
                    registerShakeSensor();
                    final View popupview_shake = in.inflate(R.layout.popup_shake, null);
                    popUpShake = true;//only on case 1, pop up shake would be true
                    shakeOpen = false;
                    pw = new PopupWindow(popupview_shake, width, height, true);
                    pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            Log.d("POPWINDOW", "onDismiss: ");

                            //turn pop shake off and reopen discover shake function
                            popUpShake = false;
                            discover_refresh = true;
                        }
                    });
                    pw.setOutsideTouchable(false);
                    pw.setAnimationStyle(R.style.popup_window_animation);
                    pw.showAtLocation(popupview_shake, Gravity.CENTER, 0, 0);
                    final ImageView shakeImg = (ImageView) popupview_shake.findViewById(R.id.pop_shake_image);
                    //looping the shake animation for popup window every 2 seconds
                    AnimationSet animation = (AnimationSet) AnimationUtils.loadAnimation(DiscoverCapsule.this, R.anim.shake);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(final Animation animation) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    shakeImg.startAnimation(animation);
                                }
                            }, 2000);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    shakeImg.startAnimation(animation);

                    button = (ImageView) popupview_shake.findViewById(R.id.dismiss);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pw.dismiss();
                        }
                    });
                    break;
                case 2:
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View slideview = inflater.inflate(R.layout.popup_slider, null);
                    final SlideValidationView slideValidationView = (SlideValidationView) slideview.findViewById(R.id.slideView);
                    final VerificationSeekBar seekbar = (VerificationSeekBar) slideview.findViewById(R.id.sb_progress);
                    pw = new PopupWindow(slideview, width, height, true);
                    pw.setOutsideTouchable(false);
                    pw.setAnimationStyle(R.style.popup_window_animation);
                    pw.showAtLocation(slideview, Gravity.CENTER, 0, 0);
                    slideValidationView.setListener(new SlideListener() {
                        @Override
                        public void onSuccess() {
                            seekbar.setProgress(0);
                            RequestSending();
                        }

                        public void onFail() {
                            displayToast(slideview.getContext(), "Almost there!Please try again", Toast.LENGTH_SHORT);
                            seekbar.setProgress(0);
                        }
                    });
                    pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            Log.d("POPWINDOW", "onDismiss: ");
                            discover_refresh = true;
                        }
                    });
                    seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            System.out.println("Current Progress" + progress);
                            slideValidationView.setOffsetX(progress);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            slideValidationView.deal();
                        }
                    });

                    button = (ImageView) slideview.findViewById(R.id.dismiss);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pw.dismiss();
                        }
                    });
            }
        }
//        Random choice = new Random();
//        int selection = choice.nextInt(3);
//        switch (selection) {
//            case 0:
//                final View popupview_tap = in.inflate(R.layout.popup_tap, null);
//                TextView hint_pop = (TextView) popupview_tap.findViewById(R.id.hint);
//                hint_pop.setText("Tap the area to open capsule");
//                pw = new PopupWindow(popupview_tap, width, height, true);
//                pw.setOutsideTouchable(false);
//                pw.setAnimationStyle(R.style.popup_window_animation);
//                pw.showAtLocation(popupview_tap, Gravity.CENTER, 0, 0);
//
//                Button button = (Button) popupview_tap.findViewById(R.id.dismiss);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        pw.dismiss();
//                    }
//                });
//
//                View img = popupview_tap.findViewById(R.id.tap_me);
//                img.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        RequestSending();
//                    }
//                });
//                break;
//            case 1:
//                registerShakeSensor();
//                final View popupview_shake = in.inflate(R.layout.popup_shake, null); //TODO: Don't set final layout, but inflate layout in each case
//                popUpShake = true;
//                shakeOpen = false;
//                TextView hint_shake = (TextView) popupview_shake.findViewById(R.id.hint);
//                hint_shake.setText("Shake slightly to open the capsule");
//                pw = new PopupWindow(popupview_shake, width, height, true);
//                pw.setOutsideTouchable(false);
//                pw.setAnimationStyle(R.style.popup_window_animation);
//                pw.showAtLocation(popupview_shake, Gravity.CENTER, 0, 0);
//
//                final ImageView shakeImg = (ImageView) popupview_shake.findViewById(R.id.pop_shake_image);
//                //looping the shake animation for popup window every 2 seconds
//                                    AnimationSet animation = (AnimationSet) AnimationUtils.loadAnimation(DiscoverCapsule.this, R.anim.shake);
//                                    animation.setAnimationListener(new Animation.AnimationListener() {
//                                        @Override
//                                        public void onAnimationStart(Animation animation) {
//
//                                        }
//
//                                        @Override
//                                        public void onAnimationEnd(final Animation animation) {
//                                            new Handler().postDelayed(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    shakeImg.startAnimation(animation);
//                                                }
//                                            },2000);
//                                        }
//
//                                        @Override
//                                        public void onAnimationRepeat(Animation animation) {
//
//                                        }
//                                    });
//                shakeImg.startAnimation(animation);
//
//                button = (Button) popupview_shake.findViewById(R.id.dismiss);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        pw.dismiss();
//                        popUpShake = false;
//                    }
//                });
//                break;
//            case 2:
//                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                final View slideview = inflater.inflate(R.layout.popup_slider, null);
//                final SlideValidationView slideValidationView = (SlideValidationView) slideview.findViewById(R.id.slideView);
//                final VerificationSeekBar seekbar = (VerificationSeekBar) slideview.findViewById(R.id.sb_progress);
//                pw = new PopupWindow(slideview, width, height, true);
//                pw.setOutsideTouchable(false);
//                pw.setAnimationStyle(R.style.popup_window_animation);
//                pw.showAtLocation(slideview, Gravity.CENTER, 0, 0);
//
//                slideValidationView.setListener(new SlideListener() {
//                    @Override
//                    public void onSuccess() {
//                        Toast.makeText(slideview.getContext(), "Success!", Toast.LENGTH_SHORT).show();
//                        seekbar.setProgress(0);
//                        RequestSending();
//                    }
//
//                    public void onFail() {
//                        Toast.makeText(slideview.getContext(), "Fail!Try again", Toast.LENGTH_SHORT).show();
//                        seekbar.setProgress(0);
//                    }
//                });
//                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        System.out.println("Current Progress" + progress);
//                        slideValidationView.setOffsetX(progress);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                        slideValidationView.deal();
//                    }
//                });
//
//        }
    }

    public void RequestSending() {
//        discover_refresh=true;
        pw.dismiss();
        final ProgressDialog progress = new ProgressDialog(DiscoverCapsule.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait for server verficiation");
        progress.show();
        Double lon = curLon;
        Double lat = curLat;
        String token = UserUtil.getToken(DiscoverCapsule.this);
        Log.d("PopupWindow", "onMarkerClick: " + "Longtitude is " + lon + "The latitude is" + lat);
        Log.d("PopupWindow", "Compare with the location of last position" + mLastLocation.getLatitude() + "Longtitude is " + mLastLocation.getLongitude());
        Log.d("PopupWindow", "onMarkerClick: " + "get the information of selected capsule" + selectedCapsule.toString());
        final JSONObject request = new JSONObject();
        try {
            request.put("tkn", token);
            request.put("lat", mLastLocation.getLatitude());
            request.put("lon", mLastLocation.getLongitude());
            request.put("time", Calendar.getInstance().getTime());
            request.put("cid", selectedCapsule.get("cid"));
            Log.d("PopUpWindow", "onMarkerClick: " + "Information of request" + request.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpUtil.openCapsule(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("PopUpWindow information send", "onFailure: ");
                DiscoverCapsule.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();

                        //now enable discover shake function
                        popUpShake = false;
                        discover_refresh = true;

                        Snackbar snackbar = Snackbar
                                .make(drawerLayout, "Open capsule timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
                //Toast.makeText(getApplicationContext(),"No Internet to send request",Toast.LENGTH_SHORT);
                //pw.dismiss();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject replyJSON = new JSONObject(response.body().string());
                    Log.d("Response from server", "onResponse: " + replyJSON.toString());
                    if (replyJSON.has("success")) {
                        DiscoverCapsule.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //remove marker after user opens the capsule
                                selectedMarker.remove();
                                mCapsuleMarkers.remove(selectedMarker);

                                progress.dismiss();
                                //now enable discover shake function
                                popUpShake = false;
                                discover_refresh = true;

                                displayToast(DiscoverCapsule.this, "Success! Wait for loading capsule!", Toast.LENGTH_SHORT);

                                Intent intent = new Intent(DiscoverCapsule.this, Display.class);
                                intent.putExtra("capsule", selectedCapsule.toString());
                                startActivity(intent);
                                overridePendingTransition(R.anim.pop_up,R.anim.stay);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayToast(Context context, String msg, int length){
        if (toast == null || !toast.getView().isShown()){
            toast = Toast.makeText(context, msg, length);
            toast.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("shake", "onResume from Discover: called, registerShakeSensor");
        registerShakeSensor();
    }

    @Override
    public void onPause() {
        Log.d("shake", "onPause from Discover: called, unregisterListener");
        sensorMgr.unregisterListener(this);
        popUpShake = false;

        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("shake", "onDestroy from Discover: called, unregisterListener");
        // unregister a listener from the shake sensor
        sensorMgr.unregisterListener(this);

        super.onDestroy();
    }
}

/* HTTP GET Method
 Returns
    {"sucess": true,
     "capsules": [
            {"cid": 1,
            "cusr": "test",
            "ccontent": "Test content",
            "ctitle": "Test Title",
            "cimage": null,
            "caudio": null,
            "ccount": 0,
            "clat": -37.813629,
            "clon": 144.963058,
            "cpermission": 1, where 0 means private and 1 means public
            "cavatar": null},
            {"cid": 2,
            "cusr": "test",
            "ccontent": "Test content1",
            "ctitle": "Test Title1",
            "cimage": null,
            "caudio": null,
            "ccount": 0,
            "clat": -37.813629,
            "clon": 144.963058,
            "cpermission": 1,
            "cavatar": null}
        ]
    }
}*/