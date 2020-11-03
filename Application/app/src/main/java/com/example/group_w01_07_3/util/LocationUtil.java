package com.example.group_w01_07_3.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationUtil {

    private final int REQUEST_PERMISSION_COARSE_LOCATION = 2;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private LocationManager lm;
    private AppCompatActivity context;
    private Location lo;
    private FusedLocationProviderClient fusedLocationClient;

    public LocationUtil(AppCompatActivity activity) {
        context = activity;
        lm = (LocationManager) context.getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    private boolean checkLocationPermission() {
        int fineLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
        int corseLocation = ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (corseLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    public Location getLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(context, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lo = location;
                                Log.i("location", location.toString());
                                // Logic to handle location object
                            } else {
                                Log.i("location error", "Cannot get location");
                            }
                        }
                    });
            return lo;
        } else {
            Toast.makeText(context.getApplicationContext(), "Need permission", Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
    }

}