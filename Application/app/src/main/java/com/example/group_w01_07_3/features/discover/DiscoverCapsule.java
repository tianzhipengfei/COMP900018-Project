package com.example.group_w01_07_3.features.discover;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

    boolean doubleBackToExitPressedOnce = false;
    private String usernameProfileString;

    // receive capsule information through HTTP GET request
    JSONArray allCapsules = new JSONArray();
    JSONObject selectedCapsule = new JSONObject();
    // request capsule
    private JSONObject capsuleInfo = new JSONObject();

    private Toolbar mToolbar;
    private DrawerLayout drawerLayout;
    View headerview;
    TextView headerUsername;

    NavigationView navigationView;

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mCapsuleLocationMarker;
    List<Marker> mCapsuleMarkers = new ArrayList<Marker>();
    FusedLocationProviderClient mFusedLocationClient;
    private boolean updateCameraFlag = true;
    final int PER_SECOND = 1000;
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
    // shake event
    private SensorManager sensorMgr;
    private float last_x = 0 ;
    private float last_y = 0;
    private float last_z = 0;
    private static final int SHAKE_THRESHOLD = 800;
    private long lastUpdate = System.currentTimeMillis();

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

        try {
            capsuleInfo.put("max_distance", 1000);
            capsuleInfo.put("num_capsules", 3);
        } catch (JSONException e) {
            System.out.print("Problems happen during parsing json objects");
        }

        Toast.makeText(DiscoverCapsule.this,
                "Let's look for capsules nearby! Shake to refresh capsules", Toast.LENGTH_SHORT).show();

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mLocationRequest = new LocationRequest();
        // the location will be updated every locationUpdateInterval second
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
            } else {
                // request location permission
                checkLocationPermission();
                Log.i("MGOOGLEMAP:", "checkLocationPermission");
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }

        // make markers clickable
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.w("Click", "onMarkerClick:" + marker);
                for (Marker m : mCapsuleMarkers) {
                    Log.w("Click", "one of mCapsuleLocationMarker is clicked:" + m);
                    if (marker.equals(m)) {
                        Log.w("Click", "******* popup window *******");
                        //Todo: add popupWindow()
                        LayoutInflater in=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View popupview=in.inflate(R.layout.popup_window_layout,null);
                        int width= LinearLayout.LayoutParams.WRAP_CONTENT;
                        int height=LinearLayout.LayoutParams.WRAP_CONTENT;
                        final PopupWindow pw=new PopupWindow(popupview,width,height,true);
                        pw.showAtLocation(popupview, Gravity.CENTER,0,0);
                        ImageView img=popupview.findViewById(R.id.tap_me);
                        LocationUtil currentLocation=new LocationUtil(DiscoverCapsule.this);
                        Location current_Location=currentLocation.getLocation();
                        //get the required information to send the request to the server
                        Double lon=current_Location.getLatitude();
                        Double lat=current_Location.getAltitude();
                        String token=UserUtil.getToken(DiscoverCapsule.this);
                        Log.d("PopupWindow", "onMarkerClick: "+"Longtitude is "+lon+"The latitude is"+lat);
                        Log.d("PopupWindow","Compare with the location of last position"+mLastLocation.getLatitude()+"Longtitude is "+mLastLocation.getLongitude());
                        Log.d("PopupWindow", "onMarkerClick: "+"get the information of selected capsule"+selectedCapsule.toString());
                        final JSONObject request=new JSONObject();
                        try {
                            request.put("tkn",token);
                            request.put("lat",mLastLocation.getLatitude());
                            request.put("lon",mLastLocation.getLongitude());
                            request.put("time", Calendar.getInstance().getTime());
                            request.put("cid",selectedCapsule.get("cid"));
                            Log.d("PopUpWindow", "onMarkerClick: "+"Information of request"+request.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String capsuleInfo="{\"cid\":7,\"cusr\":\"lyt\",\"ccontent\":\"THE FIFTH CAPSULE\",\"ctitle\":\"THE FIFTH CAPSILE\",\"cimage\":\"https:\\/\\/file.example.vn\\/images\\/file_example_JPG_500kB.jpg\",\"caudio\":\"https:\\/\\/www.soundhelix.com\\/examples\\/mp3\\/SoundHelix-Song-2.mp3\",\"ccount\":0,\"clat\":37.429025,\"clon\":-122.083288,\"cpermission\":1,\"cavatar\":\"https:\\/\\/www.tianzhipengfei.xin\\/static\\/mobile\\/lyt-1603078706.jpg\"}";
                                try {
                                    JSONObject capsuleTry=new JSONObject(capsuleInfo);
                                    pw.dismiss();
                                    Intent intent=new Intent(DiscoverCapsule.this, Display.class);
                                    intent.putExtra("capsule",capsuleTry.toString());
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("Pop up window message send", "onClick: ");
//                                HttpUtil.openCapsule(request, new Callback() {
//                                    @Override
//                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                                        Log.d("PopUpWindow information send", "onFailure: ");
//                                        DiscoverCapsule.this.runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Toast.makeText(getApplicationContext(),"No Internet to send request",Toast.LENGTH_SHORT);
//                                                pw.dismiss();
//                                            }
//                                        });
//                                        //Toast.makeText(getApplicationContext(),"No Internet to send request",Toast.LENGTH_SHORT);
//                                        //pw.dismiss();
//                                    }
//                                    @Override
//                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                                        try {
//                                            JSONObject replyJSON=new JSONObject(response.body().string());
//                                            Log.d("Response from server", "onResponse: "+replyJSON.toString());
//                                            if (replyJSON.has("success")){
//                                                DiscoverCapsule.this.runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        Toast.makeText(DiscoverCapsule.this,"Success! Wait for loading capsule!",Toast.LENGTH_SHORT);
//                                                        pw.dismiss();
//                                                        Intent intent=new Intent(DiscoverCapsule.this, Display.class);
//                                                        intent.putExtra("capsule",selectedCapsule.toString());
//                                                        startActivity(intent);
//                                                        finish();
//                                                    }
//                                                });
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
                            }
                        });
                        //remove the capsule marker in google map after an user opens the capsule
                        m.remove();
                        return true;
                    }
                }
                if (marker.equals(mCurrLocationMarker)) {
                    Log.w("Click", "mCurrLocationMarker is clicked");
                }
                return false;
            }
        });
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

                Log.d("CAPSULEMARKER", "allCapsules: " + allCapsules);
                Log.d("CAPSULEMARKER", "allCapsules.length(): " + allCapsules.length());
                //place capsule marker
                for (int i = 0; i < allCapsules.length(); i++) {
                    try {
                        JSONObject objects = allCapsules.getJSONObject(i);
                        Double lat = objects.getDouble("clat");
                        Double lng = objects.getDouble("clon");
                        Log.d("CAPSULEMARKER", "i: " + i);
                        Log.d("CAPSULEMARKER", "lat: " + lat);
                        Log.d("CAPSULEMARKER", "lng: " + lng);

                        //show capsules on the map when user is nearby that particular area
                        // Latitude: 1 deg = 110.574 km; Longitude: 1 deg = 111.320*cos(latitude) km
                        if (Math.abs(location.getLatitude() - lat) < 0.04 &&
                                Math.abs(location.getLongitude() - lng) < 0.04) {
                            LatLng lat_Lng = new LatLng(lat, lng);
                            MarkerOptions capsuleMarker = new MarkerOptions();
                            capsuleMarker.position(lat_Lng);
                            capsuleMarker.title("Capsule");
                            capsuleMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            mCapsuleLocationMarker = mGoogleMap.addMarker(capsuleMarker);
                            mCapsuleMarkers.add(mCapsuleLocationMarker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("CAPSULEMARKER", "markerOptions2-error: " + allCapsules);
                    }
                }

                //google map zoom in and zoom out
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

                //always show marker title
                mCurrLocationMarker.showInfoWindow();
                for (Marker m: mCapsuleMarkers ) {
                    m.showInfoWindow();
                }

                if (checkForRequest(location.getLatitude(), location.getLongitude())) {
                    // TODO: send request
                    lastRequestLat = location.getLatitude();
                    lastRequestLon = location.getLongitude();
                    updateCameraFlag = true;
                    try {
                        capsuleInfo.put("lat", lastRequestLat);
                        capsuleInfo.put("lon", lastRequestLon);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //move map camera
                if (updateCameraFlag) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    updateCameraFlag = false;
                    Toast.makeText(DiscoverCapsule.this,
                            "Let's look for capsules nearby! Shake to refresh capsules", Toast.LENGTH_SHORT).show();
                }
            }

            if (capsuleInfo.length() == 0) {
                Toast.makeText(DiscoverCapsule.this, "No token to get capsule", Toast.LENGTH_SHORT).show();
                Log.d("CAPSULE", "***** No token to get capsule *****");
                allCapsules = new JSONArray();
                selectedCapsule = new JSONObject();
            } else {
                try {
                    String token = UserUtil.getToken(DiscoverCapsule.this);
                    Log.i(" CAPSULE", "token:" + token);
                    Log.i(" CAPSULE", "capsuleInfo:" + capsuleInfo);
                    HttpUtil.getCapsule(token, capsuleInfo, new Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            Log.d("CAPSULE", "***** getCapsule onResponse *****");
                            String responseData = response.body().string(); //.getClass().getName() java.lang.String
                            // {"sucess": true, "capsules": [{dictItem, dictItem}, {dictItem, dictItem}]}
                            Log.i("CAPSULE", "responseData:" + responseData);

                            try {
                                JSONObject responseJSON = new JSONObject(responseData);
                                if (responseJSON.has("success")) {
                                    String status = responseJSON.getString("success");
                                    Log.d("CAPSULE", "getCapsule success: " + status);

                                    allCapsules = responseJSON.getJSONArray("capsules");
                                    Log.d("CAPSULE", "capsuleInfo: " + allCapsules);

                                    Random rand = new Random();
                                    selectedCapsule = allCapsules.getJSONObject(rand.nextInt(allCapsules.length()));
                                    Log.d("CAPSULE", "selectedCapsule: " + selectedCapsule);
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
        if(drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawer(navigationView);
        }else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    // detect a shake event and the shake direction
    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = values[SensorManager.DATA_X];
                float y = values[SensorManager.DATA_Y];
                float z = values[SensorManager.DATA_Z];
                // shaking speed
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("SHAKE-EVENT", "shake detected w/ speed: " + speed);
                    // Todo: comment out toast message after testing,
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
}

/* HTTP GET Method
 Returns
    {"sucess": true,
     "capsules": [
         {"cid": 2, "cusr": "test1",
         "ccontent": "Test content1",
          "ctitle": "Test title",
          "cimage": null,
          "caudio": null,
          "ccount": 0, "cavatar": null},
         {"cid": 3,
          "cusr": "test1",
          "ccontent": "Test content2",
          "ctitle": "Test title",
          "cimage": null,
          "caudio": null,
          "ccount": 0,
          "cavatar": null
         }
        ]
    }
}*/
