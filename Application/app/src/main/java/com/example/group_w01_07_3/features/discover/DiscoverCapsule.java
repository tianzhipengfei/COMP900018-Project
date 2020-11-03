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
import com.example.group_w01_07_3.SignIn;
import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.util.FeedbackUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.MessageUtil;
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
    private boolean can_i_shake = false;
    private boolean can_i_retrieve_http = true;
    private boolean can_i_fresh_markers = false;
    // google map's location permission
    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private final int PER_SECOND = 1000; // (unit: ms)
    private int locationUpdateInterval = 5 * PER_SECOND;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFrag;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    // last requested location
    private Location mLastLocation;
    private double lastRequestLat = 360.0;
    private double lastRequestLon = 360.0;
    private long lastUpdate_time;
    private double curLat = 360.0;
    private double curLon = 360.0;
    // distance capsules request interval (unit: degree)
    private double distanceThresholdToRequest = 5.55;
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
    private static final int MAX_PAUSE_BETWEEN_SHAKES = 200;  // (unit: ms)
    private static final int NUM_SHAKES = 5;
    private static final float COSINE = (float) 0.5; // rotation angle
    private static final float FORCE_THRESHOLD = (float) 10; // rotation force threshold
    // pop-up window
    private boolean popUpShake = false;
    private PopupWindow pw;
    boolean doubleBackToExitPressedOnce = false;
    private View popupview_tap;
    private View popupview_shake;
    private View popview_slide;

    /**
     * Performs basic application startup logic that happens only once for the entire life of the activity.
     *
     * @param savedInstanceState a Bundle object containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_capsule);

        initView();
        updateHeader();

        // main entry point for location services integration
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        registerShakeSensor();
    }

    /**
     * Initialise navigation view
     */
    private void initView() {
        //use toolbar at top of screen across all activities
        Toolbar toolbar = findViewById(R.id.toolbar_discover);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shake Phone & Discover");

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
    }

    /**
     * Handle navigation drawer item click event, which navigates user to the destination
     *
     * @param item the top level-destination listed at the navigation drawer
     * @return a boolean indicates if menu item click is done
     */
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

    /**
     * Update drawer layout header username & avatar. Supports auto retry via OKHTTP
     */
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
                        } else if (responseJSON.has("error")) {
                            String status = responseJSON.getString("error");
                            Log.d("PROFILE", "getProfile error: " + status);
                            DiscoverCapsule.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    UserUtil.clearToken(DiscoverCapsule.this);
                                    MessageUtil.displayToast(DiscoverCapsule.this, "Not logged in", Toast.LENGTH_SHORT);
                                    Intent intent = new Intent(DiscoverCapsule.this, SignIn.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    /**
     * Called when the map is ready to be used.
     *
     * @param googleMap a non-null instance of a GoogleMap that defines the callback
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        checkLocationPermission();
        mapLayout();
    }

    /**
     * Checks location permissions for LocationManager.
     */
    private void checkLocationPermission() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationUpdateInterval);
        mLocationRequest.setFastestInterval(locationUpdateInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // enables the my-location layer because the permission was granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                // request location permission
                requestLocationPermission();
            }
        } else {
            // enables the my-location layer because the permission was granted
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    /**
     * Requests location permissions at runtime
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    /**
     * Callback for receiving the results for permission requests.
     *
     * @param requestCode  the request intent that came back
     * @param permissions  the requested permissions
     * @param grantResults the grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        // restart DiscoverCapsule activity if the location permission was granted
                        finish();
                        Intent intent = new Intent(DiscoverCapsule.this, DiscoverCapsule.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } else {
                    MessageUtil.displaySnackbar(drawerLayout, "Location permission denied...", Snackbar.LENGTH_LONG);
                }
                return;
            }
        }
    }

    /**
     * Organizes map information so that it clearly communicates that information to the map's audience.
     */
    private void mapLayout() {
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            /**
             * Clicks a capsule marker to open a popup window
             *
             * @param marker a clickable capsule marker
             * @return true if one of the capsule markers is clicked, otherwise false.
             */
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

    /**
     * Used for receiving notifications when the device location has changed or can no longer be determined
     */
    final LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            // request capsule information using users' current locations
            updateCapsuleRequestLocation(locationResult);
            requestCapsuleInfo();

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                // get current location
                Location location = locationList.get(locationList.size() - 1);
                check_ifCanRefreshMarkers();
                drawGoogleMapLayout(location);
            }
        }
    };

    /**
     * Stores user's current location in 'capsuleInfo' which will be later used to make an HTTP get request
     *
     * @param locationResult a user's location history
     */
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
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 16));
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

    /**
     * Checks if user's current location is further than the location when he last refreshed capsule.
     *
     * @param curLat current latitude
     * @param curLon current longitude
     * @return true if distance threshold is met, otherwise false.
     */
    private boolean checkForRequest(double curLat, double curLon) {
        if (lastRequestLat == 360 && lastRequestLon == 360) {
            return true;
        } else if (getDistance(curLat, curLon, lastRequestLat, lastRequestLon) > distanceThresholdToRequest) {
            System.out.println(getDistance(curLat, curLon, lastRequestLat, lastRequestLon));
            return true;
        }
        return false;
    }

    /**
     * Calculates the distance between two locations by latitude and longitude (unit: Kilometers)
     *
     * @param lat1 latitude1
     * @param lon1 longitude1
     * @param lat2 latitude2
     * @param lon2 longitude2
     * @return distance value
     */
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

    /**
     * Makes an HTTP get request to retrieve capsule information.
     */
    private void requestCapsuleInfo() {

        // Only request capsule from server if discover is still alive
        // handled the case if switch between activity, then we will have a stack of capsule list to updated
        // no matter online or offline
        if (!this.isDestroyed()) {
            if (capsuleInfo.length() == 0) {
                MessageUtil.displaySnackbar(drawerLayout, "Discover nearby Geo-capsule Failed. No user token to proceed.", Snackbar.LENGTH_SHORT);
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
                                } else if (responseJSON.has("error")) {
                                    String status = responseJSON.getString("error");
                                    Log.d("GETCAPSULE", "getCapsule error: " + status);
                                    DiscoverCapsule.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            UserUtil.clearToken(DiscoverCapsule.this);
                                            MessageUtil.displayToast(DiscoverCapsule.this, "Not logged in", Toast.LENGTH_SHORT);
                                            Intent intent = new Intent(DiscoverCapsule.this, SignIn.class);
                                            startActivity(intent);
                                            finish();
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
                            MessageUtil.displaySnackbar(drawerLayout, "Discover nearby Geo-capsule Failed. Retry in 3 seconds.", Snackbar.LENGTH_LONG);
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

    /**
     * Checks if a capsule refresh condition has been met.
     */
    private void check_ifCanRefreshMarkers() {

        //Same reason, do not drawer marker if discover activity has been finished
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

    /**
     * Places capsule markers on google maps after receiving the latest capsule information via a HTTP get request.
     *
     * @param allCapsules capsule information
     */
    public void refreshMarkers(JSONArray allCapsules) {
        mGoogleMap.clear();
        Log.d("CAPSULEMARKER", "allCapsules: " + allCapsules);

        int num_capsules = 0;

        //place capsule markers on google maps
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

            num_capsules += 1;
            Log.d("CAPSULEMARKER", "refresh_counts: " + num_capsules);
        }

        registerShakeSensor();
    }

    /**
     * Redraws the layout google maps including buttons and markers.
     *
     * @param location user's current location
     */
    private void drawGoogleMapLayout(Location location) {
        // default location Googleplex: 37.4219983 latitude -122.084 longitude
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

    /**
     * Sets up sensor manager.
     */
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

    /**
     * Called when the values of shake sensor have changed.
     *
     * @param sensor the ID of the sensor being monitored
     * @param values the values of x,y,z for the shake sensor
     */
    @Override
    public void onSensorChanged(int sensor, float[] values) {
        //when detect 10 times of slight shake, open the capsule
        if (open_shake_time == NUM_SHAKES) {
            sensorMgr.unregisterListener(this);

            open_shake_time = 0;
            shakeOpen = true;
            if (popUpShake) {
                FeedbackUtil.vibrate(this);
                RequestSending();
            } else {
                FeedbackUtil.vibrate(this);
                MessageUtil.displaySnackbar(drawerLayout, "Discovering Memory Capsule...Please Wait", Snackbar.LENGTH_LONG);
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

    /**
     * Counts the number of shakes.
     * The logic is studied from https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android, with own perfection
     *
     * @param values the values of x,y,z for the shake sensor
     */
    private void addShakeSuccessCount(float[] values) {
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

    /**
     * Initializes new popup window with inflated view, width, height and location, add dismiss listener.
     *
     * @param view   inflated view of the popup window
     * @param width  width of popup window
     * @param height height of popup window
     */
    private void initPopWindow(View view, int width, int height) {
        pw = new PopupWindow(view, width, height, true);
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
        pw.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    /**
     * The listener receive click event from image view, then dismiss the popup window
     *
     * @param dismiss image view allows for user's click
     */
    private void initDismissListener(ImageView dismiss) {
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pw.dismiss();
            }
        });
    }


    /**
     * Adds listener to verfication image view of popup window-slider, check the position of captcha
     * matches the position of puzzle
     *
     * @param slideValidationView image of puzzle
     * @param seekbar             the slider with draggable thumb
     */
    private void initSlideVerificationListener(final SlideValidationView slideValidationView, final VerificationSeekBar seekbar) {
        slideValidationView.setListener(new SlideListener() {

            /**
             * Check if the position of thumb equals to position of puzzle, send request to server, place
             * thumb back to start position.
             */
            @Override
            public void onSuccess() {
                seekbar.setProgress(0);
                RequestSending();
            }

            /**
             * Check if position of thumb does not match position of puzzle, display fail message to user
             * place thumb back to start position.
             */
            public void onFail() {
                MessageUtil.displaySnackbar(drawerLayout, "Puzzle completion failed. Please try again.", Snackbar.LENGTH_SHORT);
                seekbar.setProgress(0);
            }
        });
        //add listener to listener to drag of thumb of seekbar
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Notifies clients when the progress level of seekbar has been changed
             *
             * @param seekBar  progress bar used for verification
             * @param progress The current progress level
             * @param fromUser true if the progress change was initiated by the user
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("Current Progress" + progress);
                slideValidationView.setOffsetX(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            /**
             * Checks if user pass the verification, when the user has finished a touch gesture.
             * @param seekBar the SeekBar in which the touch gesture began
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                slideValidationView.deal();
            }
        });
    }

    /**
     * Initializes animation for shake popup window
     *
     * @param shakeImg image view of shake popup window
     */
    private void initShakeImageAnimation(final ImageView shakeImg) {
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
    }

    /**
     * Creates a new pop-up window randomly from three type: tap a clickable image area,shake phone
     * and slide seekbar to match puzzle.
     */
    public void PopUpWindowFunction() {
        //first thing to do is disable shake function for discover activity when window is poped
        discover_refresh = false;

        Log.w("MARKERS-MATCH", "******* FIRE POP WINDOW*******");
        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView dismiss;
        final ImageView shakeImg;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        int width = (getWindowManager().getDefaultDisplay().getWidth() * 3) / 4;
        if (pw != null && pw.isShowing()) {
            Log.d("popwindow", "PopUpWindowFunction: one window already showing, don't pop another");
        } else {
            //choose a type of pop-up window randonly
            Random choice = new Random();
            int selection = choice.nextInt(3);
            switch (selection) {
                case 0:
                    //create a pop-up window with a clickable area
                    popupview_tap = in.inflate(R.layout.popup_tap, null);
                    dismiss = (ImageView) popupview_tap.findViewById(R.id.dismiss);
                    View img = popupview_tap.findViewById(R.id.tap_me);
                    initPopWindow(popupview_tap, width, height);
                    initDismissListener(dismiss);
                    img.setOnClickListener(new View.OnClickListener() {
                        /**
                         *Invokes when a view is clicked, open the capsule successfully.
                         * @param view clickable image
                         */
                        @Override
                        public void onClick(View view) {
                            RequestSending();
                        }
                    });
                    break;
                case 1:
                    //create popup window that need shake from user
                    //register shake listener to listen to shake form user
                    registerShakeSensor();
                    popupview_shake = in.inflate(R.layout.popup_shake, null);
                    dismiss = (ImageView) popupview_shake.findViewById(R.id.dismiss);
                    shakeImg = (ImageView) popupview_shake.findViewById(R.id.pop_shake_image);
                    popUpShake = true;//only on case 1, pop up shake would be true
                    shakeOpen = false;
                    initPopWindow(popupview_shake, width, height);
                    initDismissListener(dismiss);
                    initShakeImageAnimation(shakeImg);
                    break;
                case 2:
                    //create popup window that uses puzzle verification
                    popview_slide = in.inflate(R.layout.popup_slider, null);
                    dismiss = (ImageView) popview_slide.findViewById(R.id.dismiss);
                    final SlideValidationView slideValidationView = (SlideValidationView) popview_slide.findViewById(R.id.slideView);
                    final VerificationSeekBar seekbar = (VerificationSeekBar) popview_slide.findViewById(R.id.sb_progress);

                    initPopWindow(popview_slide, width, height);
                    initDismissListener(dismiss);
                    initSlideVerificationListener(slideValidationView, seekbar);

            }
        }
    }

    /**
     * Send open capsule request to server, check the confidential of user and display the content
     * of capsule if the request is accepted by server
     */
    public void RequestSending() {
        pw.dismiss();
        //create a progress bar to notify user to wait for server reply
        final ProgressDialog progress = new ProgressDialog(DiscoverCapsule.this);
        progress.setTitle("Loading");
        progress.setMessage("Almost there...Opening the capsule may take a few seconds");
        progress.setCancelable(false);
        progress.show();
        Double lon = curLon;
        Double lat = curLat;
        String token = UserUtil.getToken(DiscoverCapsule.this);
        Log.d("PopupWindow", "onMarkerClick: " + "Longtitude is " + lon + "The latitude is" + lat);
        Log.d("PopupWindow", "Compare with the location of last position" + mLastLocation.getLatitude() + "Longtitude is " + mLastLocation.getLongitude());
        Log.d("PopupWindow", "onMarkerClick: " + "get the information of selected capsule" + selectedCapsule.toString());
        //create a request object containing all information for request
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
        //send request to server and handle the response from server
        HttpUtil.openCapsule(request, new Callback() {
            /**
             * Receives network failures error from server, dismiss the progress bar, notify user about
             * internet error and stay in current activity
             * @param call  a request that has been prepared for execution
             * @param e  network error
             */
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("PopUpWindow information send", "onFailure: ");
                DiscoverCapsule.this.runOnUiThread(new Runnable() {
                    /**
                     * Dismiss the progress bar, notify the user on network failure
                     */
                    @Override
                    public void run() {
                        progress.dismiss();
                        //now enable discover shake function
                        popUpShake = false;
                        discover_refresh = true;
                        MessageUtil.displaySnackbar(drawerLayout, "Open Geo-capsule timeout. Please check Internet connection", Snackbar.LENGTH_LONG);
                    }
                });
            }

            /**
             * Receives response from server, if the server reply contains success, the request is
             * approved by server, switch to display page.
             *
             * @param call   a request that has been prepared for execution
             * @param response response received from user
             * @throws IOException exception of reading reply
             */
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject replyJSON = new JSONObject(response.body().string());
                    Log.d("Response from server", "onResponse: " + replyJSON.toString());
                    //if the server approve the request
                    if (replyJSON.has("success")) {
                        DiscoverCapsule.this.runOnUiThread(new Runnable() {
                            /**
                             * Removes the marker from map, dismiss the progress bar, switch to display
                             * page if the request is approved  by server.
                             */
                            @Override

                            public void run() {
                                //remove marker after user opens the capsule
                                selectedMarker.remove();
                                mCapsuleMarkers.remove(selectedMarker);

                                progress.dismiss();

                                //now enable discover shake function
                                popUpShake = false;
                                discover_refresh = true;

                                // If no capsules on map, refresh
                                if (mCapsuleMarkers.size() == 0) {
                                    can_i_shake = false;
                                    can_i_retrieve_http = true;
                                    can_i_fresh_markers = false;
                                }

                                Intent intent = new Intent(DiscoverCapsule.this, Display.class);
                                intent.putExtra("capsule", selectedCapsule.toString());
                                startActivity(intent);
                                overridePendingTransition(R.anim.pop_up, R.anim.stay);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Called when the activity that was hidden comes back to view on the screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("shake", "onResume from Discover: called, registerShakeSensor");
        registerShakeSensor();
    }

    /**
     * Commits to the backing content provider or file any changes the user has made.
     */
    @Override
    public void onPause() {
        Log.d("shake", "onPause from Discover: called, unregisterListener");
        sensorMgr.unregisterListener(this);
        popUpShake = false;

        super.onPause();
    }

    /**
     * The final call before the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        Log.d("shake", "onDestroy from Discover: called, unregisterListener");
        // unregister a listener from the shake sensor
        sensorMgr.unregisterListener(this);

        super.onDestroy();
    }

    /**
     * Double back pressed to exit app
     * The logic is borrowed from https://stackoverflow.com/questions/8430805/clicking-the-back-button-twice-to-exit-an-activit
     */
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
            MessageUtil.displayToast(this, "Press back again to exit", Toast.LENGTH_SHORT);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

}