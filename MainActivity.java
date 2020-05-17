package com.iasonas.coronavirus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnStartTracking;
    Button btnStopTracking;
    TextView txtStatus;


    public BackgroundService gpsService;
    public boolean mTracking = false;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))

        {
            ActivityCompat.requestPermissions(this,new String[]{

                    Manifest.permission.ACCESS_FINE_LOCATION

            },REQUEST_CAMERA_PERMISSION);

        }

        btnStartTracking = (Button)findViewById(R.id.btn_start_tracking);
        btnStopTracking = (Button)findViewById(R.id.btn_stop_tracking);
        txtStatus = (TextView) findViewById(R.id.txt_status);

        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            public void onClick(View _view) {

                gpsService.startTracking();
                mTracking = true;

            }
        });

        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            public void onClick(View _view) {

                mTracking = false;
                gpsService.stopTracking();

            }
        });

        final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
        intent.putExtra("listener", new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode == Activity.RESULT_OK) {


                    String resultValue = resultData.getString("result");

                    txtStatus.setText(resultValue);

                }
            }
        });


        if(!isMyServiceRunning(BackgroundService.class)) {
            this.getApplication().startService(intent);
        }
//        this.getApplication().startForegroundService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {

        this.getApplication().unbindService(serviceConnection);
        super.onPause();
        finish();


    }

    @Override
    public void onBackPressed()
    {

        this.getApplication().unbindService(serviceConnection);

        finish();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
                btnStartTracking.setEnabled(true);
                txtStatus.setText("GPS Ready");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                gpsService = null;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CAMERA_PERMISSION)

        {

            if(grantResults[0] != PackageManager.PERMISSION_GRANTED )

            {

                Toast.makeText(this, "CAMERA PERMISSIONS REQUIRED", Toast.LENGTH_SHORT).show();

                finish();

            }

        }

    }

    private boolean isMyServiceRunning(Class<?> serviceclass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if(serviceclass.getName().equals(service.service.getClassName())) { return true; }

        }
        return false;
    }

}
