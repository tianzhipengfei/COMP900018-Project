package com.example.group_w01_07_3.features.discover;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.example.group_w01_07_3.util.LocationUtil;
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
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DiscoverCapsule extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, SensorListener {

    private boolean popUpShake = false;
    private PopupWindow pw;
    boolean doubleBackToExitPressedOnce = false;
    private String usernameProfileString;
    // randomly select a capsule through HTTP GET request
    private JSONObject selectedCapsule = new JSONObject();
    private JSONArray allCapsules;
    // request capsule
    private JSONObject capsuleInfo = new JSONObject();
    private Toolbar mToolbar;
    private DrawerLayout drawerLayout;
    private View headerview;
    private TextView headerUsername;
    private NavigationView navigationView;
    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private Marker mCapsuleLocationMarker;
    private List<Marker> old_mCapsuleMarkers = new ArrayList<Marker>();
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean updateCameraFlag = true;
    private final int PER_SECOND = 1000;
    // time interval for updating locaton
    private int locationUpdateInterval = 5 * PER_SECOND;
    // if user moves more than a threshold distance (unit: km), update capsules info
    private double distanceThresholdToRequest = 0.5;
    // latitude, and longitude of last request
    private double lastRequestLat = 360.0;
    private double lastRequestLon = 360.0;
    // maximum number of capsules to discover
    private int capsuleNum = 20;
    // maximum distance to discover (unit: km)
    private double discoverCapsuleRange = 3;
    private boolean shakeOpen = false;
    // shake event
    private SensorManager sensorMgr;
    private float last_x = 0;
    private float last_y = 0;
    private float last_z = 0;
    private static final int SHAKE_THRESHOLD = 800;
    private long lastUpdate_shaking = System.currentTimeMillis();
    // shake to refresh capsules
    private boolean if_refresh = true;
    private boolean can_shake = true;
    private boolean if_connected = false;
    private int refresh_counts = 0;
    // only allow one update every 100ms = 0.2s
    private static final int max_pause_between_shakes = 200;
    private long lastUpdate_map;
    private boolean disable_camera = true;

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

        updateHeaderUsername();

        //get current Location in GoogleMap using FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

//        try {
//            capsuleInfo.put("max_distance", 10); // 5km by default
//            capsuleInfo.put("num_capsules", 30); // 20 capsules by default
//        } catch (JSONException e) {
//            System.out.print("Problems happen during parsing json objects");
//        }

        Toast.makeText(DiscoverCapsule.this,
                "Let's look for capsules nearby! Shake to refresh capsules", Toast.LENGTH_SHORT).show();

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mGoogleMap.setMyLocationEnabled(true);
//        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        item.setChecked(true);
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

    private void updateHeaderUsername() {
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

    @Override
    public void onPause() {
        super.onPause();
        if (pw != null) {
            pw.dismiss();
            popUpShake = false;
        }
    }

    private int getRandomCow(JSONArray jsonArray) {
        int length = jsonArray.length();
        int[] array;
        array = new int[length - 1];
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public List<Marker> refreshCapsules(JSONArray allCapsules) {
        if (!if_refresh) {
            return old_mCapsuleMarkers;
        }

        List<Marker> mCapsuleMarkers = new ArrayList<Marker>();
        //place capsule marker
        Log.d("CAPSULEMARKER", "allCapsules: " + allCapsules);
        Log.d("CAPSULEMARKER", "allCapsules.length(): " + allCapsules.length());

//        // get random capsule objects from a JSON array
//        try {
//            for (int i = 0; i < 20; i++) {
//                int randumNum = getRandomCow(allCapsules);
//                JSONObject objects = allCapsules.getJSONObject(randumNum);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.d("Error", "please create more capsules. allCapsules.length():" + allCapsules.length());
//        }

        // display all capsules in google map
        for (int i = 0; i < allCapsules.length(); i++) {
            try {
                JSONObject objects = allCapsules.getJSONObject(i);
                Double lat = objects.getDouble("clat");
                Double lng = objects.getDouble("clon");
                Log.d("CAPSULEMARKER", "i: " + i);
                Log.d("CAPSULEMARKER", "lat: " + lat);
                Log.d("CAPSULEMARKER", "lng: " + lng);

                //show capsules on the map when user is nearby that particular area (5km by default)
                // Latitude: 1 deg = 110.574 km; Longitude: 1 deg = 111.320*cos(latitude) km
                LatLng lat_Lng = new LatLng(lat, lng);
                MarkerOptions capsuleMarker = new MarkerOptions();
                capsuleMarker.position(lat_Lng);
                capsuleMarker.title("Capsule");

                //change marker color
                if (i == 0 || i == 10)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                if (i == 1 || i == 11)
                    capsuleMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                if (i == 2 || i == 12)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                if (i == 3 || i == 13)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                if (i == 4 || i == 14)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                if (i == 5 || i == 15)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                if (i == 6 || i == 16)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                if (i == 7 || i == 17)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                if (i == 8 || i == 18)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                if (i == 9 || i == 19)
                    capsuleMarker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                mCapsuleLocationMarker = mGoogleMap.addMarker(capsuleMarker);
                mCapsuleMarkers.add(mCapsuleLocationMarker);

                refresh_counts += 1;
                Log.d("CAPSULEMARKER", "refresh_counts: " + refresh_counts);
                Log.d("CAPSULEMARKER", "updated mCapsuleMarkers: " + mCapsuleMarkers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //always show marker title
        mCurrLocationMarker.showInfoWindow();
        if (mCapsuleMarkers.size() > 0) {
            for (Marker m : mCapsuleMarkers) {
                m.showInfoWindow();
            }
        }

        if (!old_mCapsuleMarkers.equals(mCapsuleMarkers)) {
            if_refresh = false;
            can_shake = true;
        }

        old_mCapsuleMarkers = mCapsuleMarkers;
        return mCapsuleMarkers;
    }

    public void removeCapsulesFromMap(final List<Marker> mCapsuleMarkers) { }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 'onMapReady' only run once
        mGoogleMap = googleMap;

        // make markers clickable
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.w("BEFORE-CLICK", "mCapsuleMarkers:" + old_mCapsuleMarkers);
                Log.w("BEFORE-CLICK", "mCapsuleMarkers.size():" + old_mCapsuleMarkers.size());

                for (Marker m : old_mCapsuleMarkers) {
                    Log.w("AFTER-CLICK", "one of mCapsuleLocationMarker is clicked:" + m);
                    if (marker.equals(m)) {
                        Log.w("MARKERS-MATCH", m + "");
                        Log.w("MARKERS-MATCH", "******* popup window *******");

                        //remove this marker from the map and record after an user opens the capsule
                        marker.remove();
                        old_mCapsuleMarkers.remove(m);

                        //Todo: add popupWindow()
                        PopUpWindowFunction();

                        Log.w("AFTER-CLICK", "mCapsuleMarkers:" + old_mCapsuleMarkers);
                        return true;
                    }
                }
                if (marker.equals(mCurrLocationMarker)) {
                    Log.w("AFTER-CLICK", "mCurrLocationMarker is clicked");
                }
                return false;
            }
        });

        mGoogleMap.setPadding(0, 90, 10, 0); // left, top, right, bottom
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // the location will be updated every locationUpdateInterval second
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationUpdateInterval);
        mLocationRequest.setFastestInterval(locationUpdateInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // if location permission is already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

            } else {
                // request location permission
                checkLocationPermission();
                Log.i("MGOOGLEMAP:", "checkLocationPermission");
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    final LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            Log.i("locationList", "" + locationList);
            if (locationList.size() > 0) {
                //the last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                // default location Googleplex: 37.4219983 -122.084
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                //google map zoom in and zoom out
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

                //google map current location
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                if (checkForRequest(location.getLatitude(), location.getLongitude())) {
                    // Todo: send request
                    lastRequestLat = location.getLatitude();
                    lastRequestLon = location.getLongitude();
                    updateCameraFlag = true;

                    try {
                        capsuleInfo.put("lat", lastRequestLat);
                        capsuleInfo.put("lon", lastRequestLon);
                        Log.d("UPDATE-LOCATION", capsuleInfo + "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //move map camera to current location. 1000ms = 1 seconds
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate_map) > 1000 && disable_camera == true) {
                    mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            LatLng latLng2 = new LatLng(lastRequestLat, lastRequestLon);
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 18));
                            lastUpdate_map = System.currentTimeMillis();
                        }
                    });
                    disable_camera = false;
                }

//                if (updateCameraFlag) {
//                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
//                    updateCameraFlag = false;
//                } 

                // HTTP GET method
                if (capsuleInfo.length() == 0) {
                    Toast.makeText(DiscoverCapsule.this, "No token to get capsule", Toast.LENGTH_SHORT).show();
                    Log.d("CAPSULE", "***** No token to get capsule *****");
                    allCapsules = new JSONArray();
                    selectedCapsule = new JSONObject();
                } else {
                    try {
                        String token = UserUtil.getToken(DiscoverCapsule.this);
                        Log.i("SENDING-REQUEST", "token:" + token);
                        Log.i("SENDING-REQUEST", "capsuleInfo:" + capsuleInfo);
                        Log.i("SENDING-REQUEST", "refresh_counts:" + refresh_counts);
                        HttpUtil.getCapsule(token, capsuleInfo, new Callback() {
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                Log.d("RECEIVED-CAPSULE", "***** getCapsule onResponse *****");
                                String responseData = response.body().string(); //.getClass().getName() java.lang.String
                                // {"sucess": true, "capsules": [{dictItem, dictItem}, {dictItem, dictItem}]}
                                Log.i("RECEIVED-CAPSULE", "responseData:" + responseData);
                                try {
                                    JSONObject responseJSON = new JSONObject(responseData);
                                    if (responseJSON.has("success")) {
                                        String status = responseJSON.getString("success");
                                        Log.d("DISCOVER-CAPSULE", "getCapsule success: " + status);

                                        allCapsules = responseJSON.getJSONArray("capsules");
                                        Log.d("DISCOVER-CAPSULE", "capsuleInfo: " + allCapsules);

                                        Random rand = new Random();
                                        selectedCapsule = allCapsules.getJSONObject(rand.nextInt(allCapsules.length()));
                                        Log.d("DISCOVER-CAPSULE", "selectedCapsule: " + selectedCapsule);
                                        if_connected = true;
                                        Log.d("DISCOVER-CAPSULE", "if_connected: " + if_connected);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                                Log.d("CAPSULE", "onFailure()");
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Todo: call method to make markers clickable
                if (if_connected) {
                    removeCapsulesFromMap(refreshCapsules(allCapsules));
                } else {
                    Log.i("MGOOGLEMAP:", "No connection");
                }
                Log.i("MGOOGLEMAP:", "hi");
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //if we need to show an explanation to the user *asynchronously
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // After the user sees the explanation, try again to request the permission.
                // do not block the thread waiting for the user's response
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(DiscoverCapsule.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                //no explanation needed for the user, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // if request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // if permission was granted, do location-related task
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // if permission was denied, disable relevant functionality
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }

                return;
            }
        }
    }
    //The logic is borrowed from https://stackoverflow.com/questions/44992014/how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient

    private boolean checkForRequest(double curLat, double curLon) {
        if (lastRequestLat == 360 && lastRequestLon == 360) {
            return true;
        } else if (getDistance(curLat, curLon, lastRequestLat, lastRequestLon) > distanceThresholdToRequest) {
            System.out.println(getDistance(curLat, curLon, lastRequestLat, lastRequestLon));
            return true;
        }
        return false;
    }

    // Calculate distance by latitude, and longitude (unit: Kilometers)
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


    @Override
    protected void onResume() {
        super.onResume();
    }

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
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    // detect a shake event and the shake direction
    @Override
    public void onSensorChanged(int sensor, float[] values) {
        // Todo: do not open capsule until user keeps shaking device for at least one second

        if (sensor == SensorManager.SENSOR_ACCELEROMETER && can_shake == true) {
            long curTime = System.currentTimeMillis();
            // check if the last movement was not long ago
            if ((curTime - lastUpdate_shaking) > max_pause_between_shakes) {
                long diffTime = (curTime - lastUpdate_shaking);
                lastUpdate_shaking = curTime;

                float x = values[SensorManager.DATA_X];
                float y = values[SensorManager.DATA_Y];
                float z = values[SensorManager.DATA_Z];
                // shaking speed
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > 100 && speed < SHAKE_THRESHOLD && popUpShake && !shakeOpen) {
                    pw.dismiss();
                    shakeOpen = true;
                    RequestSending();
                }

                if (speed > SHAKE_THRESHOLD && popUpShake == false) {
                    Log.d("SHAKE-EVENT", "shake detected w/ speed: " + speed);
                    // shake to refresh capsules
                    if_refresh = true;
                    // can only detect a shake event after capsules have finished updated
                    can_shake = false;
                    // Todo: comment out toast message after testing
                    Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }
    //The logic is borrowed from https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android

    @Override
    public void onAccuracyChanged(int i, int i1) {

    }

    public void PopUpWindowFunction() {
        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupview = in.inflate(R.layout.popup_window_layout, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
//        int width=1500;
//        int height=1500;
        TextView hint = (TextView) popupview.findViewById(R.id.hint);
        Random choice = new Random();
        int selection = choice.nextInt() % 3;
        switch (selection) {
            case 0:
                hint.setText("Tap the area to open capsule");
                pw = new PopupWindow(popupview, width, height, true);
                pw.showAtLocation(popupview, Gravity.CENTER, 0, 0);
                Button button = (Button) popupview.findViewById(R.id.dismiss);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pw.dismiss();
                    }
                });
                View img = popupview.findViewById(R.id.tap_me);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RequestSending();
                    }
                });
                break;
            case 1:
                popUpShake = true;
                hint.setText("Shake slightly to open the capsule");
                pw = new PopupWindow(popupview, width, height, true);
                pw.showAtLocation(popupview, Gravity.CENTER, 0, 0);
                img = popupview.findViewById(R.id.tap_me);
                img.setVisibility(View.INVISIBLE);
                button = (Button) popupview.findViewById(R.id.dismiss);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pw.dismiss();
                        popUpShake = false;
                    }
                });
                break;
            case 2:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View slideview = inflater.inflate(R.layout.popup_slider, null);
                final SlideValidationView slideValidationView = (SlideValidationView) slideview.findViewById(R.id.slideView);
                final VerificationSeekBar seekbar = (VerificationSeekBar) slideview.findViewById(R.id.sb_progress);
                pw = new PopupWindow(slideview, width, height, true);
                pw.showAtLocation(slideview, Gravity.CENTER, 0, 0);
                slideValidationView.setListener(new SlideListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(slideview.getContext(), "Success!", Toast.LENGTH_SHORT).show();
                        seekbar.setProgress(0);
                        RequestSending();
                    }

                    public void onFail() {
                        Toast.makeText(slideview.getContext(), "Fail!Try again", Toast.LENGTH_SHORT).show();
                        seekbar.setProgress(0);
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

        }
    }

    public void RequestSending() {
        Toast.makeText(this, "Congradulation! The capsule will open!", Toast.LENGTH_SHORT).show();
        LocationUtil currentLocation = new LocationUtil(DiscoverCapsule.this);
        Location current_Location = currentLocation.getLocation();
        Double lon = current_Location.getLatitude();
        Double lat = current_Location.getAltitude();
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
        popUpShake = true;
        pw.dismiss();
        HttpUtil.openCapsule(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("PopUpWindow information send", "onFailure: ");
                DiscoverCapsule.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DiscoverCapsule.this, "No Internet to send request", Toast.LENGTH_SHORT);
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
                                Toast.makeText(DiscoverCapsule.this, "Success! Wait for loading capsule!", Toast.LENGTH_SHORT);
                                pw.dismiss();
                                Intent intent = new Intent(DiscoverCapsule.this, Display.class);
                                intent.putExtra("capsule", selectedCapsule.toString());
                                startActivity(intent);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
