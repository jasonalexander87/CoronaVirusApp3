package com.iasonas.coronavirus;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class BackgroundService extends Service {

    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundService";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private Thread backgroundThread;
    ResultReceiver rec;
    boolean isrunning = false;
    boolean flag = true;
    String data;
    Location currentLocation = null;
    private final int LOCATION_INTERVAL = 2000;
    private final int LOCATION_DISTANCE = 0;
    int counter = 0;
    String LocData[] = new String[30*60];


    private Runnable myTask = new Runnable() {
        public void run() {


            while(isrunning) {

                if(currentLocation != null) {
                    double longitude = currentLocation.getLongitude();
                    double latitude = currentLocation.getLatitude();
                    long timesTamp = currentLocation.getTime();

                    String longitudeS = String.valueOf(longitude);
                    String latitudeS = String.valueOf(latitude);
                    String timeStampS = String.valueOf(timesTamp);

                    LocData[counter] = longitudeS + "/" + latitudeS + "/" + timeStampS + "|";
                }

                if(counter == 30*60) {
                    counter = 0;
                    saveToFile(LocData);
                }

                while (flag) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        }
    };

    private void saveToFile(String[] locData) {

        Intent saveFile = new Intent(this, FileSave.class);
        saveFile.putExtra("data", locData);

        startService(saveFile);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener
    {
        private Location lastLocation = null;
        private final String TAG = "LocationListener";
        private Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {

            counter++;

            currentLocation = location;

            flag=false;


           /* Bundle bundle = new Bundle();
            bundle.putString("result", cout + locS);
            rec.send(Activity.RESULT_OK, bundle);
*/
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        rec = intent.getParcelableExtra("listener");

        this.backgroundThread.start();
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {

        this.backgroundThread = new Thread(myTask);

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        Log.i(TAG, "onCreate");
        startForeground(12345678, notification);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener );

        } catch (java.lang.SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    public void stopTracking() {

        isrunning = false;
        saveToFile(LocData);
        this.onDestroy();
    }




    public class LocationServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

}