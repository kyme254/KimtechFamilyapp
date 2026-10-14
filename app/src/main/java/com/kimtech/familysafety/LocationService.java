package com.kimtech.familysafety;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {

    private static final String CHANNEL_ID =
            "kimtech_location_channel";

    private static final int NOTIFICATION_ID = 2001;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(
                    this,
                    CHANNEL_ID
            )
                    .setContentTitle("Kimtech Family Safety")
                    .setContentText(
                            "Location service is active."
                    )
                    .setSmallIcon(
                            android.R.drawable.ic_menu_mylocation
                    )
                    .setOngoing(true)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        }

        if (!hasLocationPermission()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startLocationUpdates();

        return START_NOT_STICKY;
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            stopSelf();
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Location updates are received locally.
                // Upload only after explicit consent and after
                // the server's secure sync contract is verified.
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Deprecated
            @Override
            public void onStatusChanged(
                    String provider,
                    int status,
                    Bundle extras
            ) {
            }
        };

        try {
            if (checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        60000L,
                        25f,
                        locationListener
                );

            } else if (checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        60000L,
                        25f,
                        locationListener
                );
            }

        } catch (SecurityException e) {
            stopSelf();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Location service",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Shows when the user-enabled location service is active."
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    }
