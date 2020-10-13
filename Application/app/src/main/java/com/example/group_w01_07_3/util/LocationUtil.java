package com.example.group_w01_07_3.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LocationUtil {
    private AppCompatActivity context;
    LocationManager lm;
    private final int REQUEST_PERMISSION_COARSE_LOCATION = 2;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    public LocationUtil(AppCompatActivity activity) {
        context = activity;
        lm = (LocationManager) context.getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);

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
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return location;
        }else{
            Toast.makeText(context.getApplicationContext(), "Need permission", Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
    }

}
