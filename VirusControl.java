package com.iasonas.coronavirus;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class VirusControl extends IntentService {

    int timeThreshHold = 10000;
    int spaceThreshHold = 5;

    public VirusControl() { super("test-VirusControl"); }

    @Override
    protected void onHandleIntent(Intent intent) {

        String[] myLocationEnum = null;

        try {
            File myData = new File("locData.txt");
            FileInputStream in = new FileInputStream(myData);

            int fileLenght = (int) myData.length();
            byte[] dataBytes = new byte[fileLenght];

            in.read(dataBytes);
            String myLocations = new String(dataBytes);

            myLocationEnum = myLocations.split("|");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String path = Environment.getExternalStorageDirectory().toString() + "/CoronaVirus";
        File directory = new File(path);
        File[] files = directory.listFiles();


        for(int i=0; i<files.length; i++) {

            String[] redLocationEnum = null;

            try {
                File redData = files[i];
                FileInputStream in = new FileInputStream(redData);

                int fileLenght = (int) redData.length();
                byte[] dataBytes = new byte[fileLenght];

                in.read(dataBytes);
                String redLocations = new String(dataBytes);

                redLocationEnum = redLocations.split("|");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            crossCheckLocations(myLocationEnum, redLocationEnum);

        }

    }

    private void crossCheckLocations(String[] myLocation, String[] peerLocation) {

        for(int i=0; i<myLocation.length; i++) {
            for(int j=0; j<peerLocation.length; j++) {
                boolean inContact = control(peerLocation[j],myLocation[i]);
                if(inContact) {
                    doSomething();
                }
            }
        }

    }
    private void doSomething(){
        //DOSOMETHING IF CORELATIOS IS DETECTED
    }
    private boolean control(String my, String peer) {

        String[] myCords = my.split("/");
        String[] peerCords = peer.split("/");

        double myLongitude = Double.valueOf(myCords[0]);
        double myLatitude = Double.valueOf(myCords[1]);
        long myTimestamp = Long.valueOf(myCords[2]);

        double peerLongitude = Double.valueOf(peerCords[0]);
        double peerLatitude = Double.valueOf(peerCords[1]);
        long peerTimestamp = Long.valueOf(peerCords[2]);

        long timeCorelation = Math.abs(peerTimestamp-myTimestamp);
        float[] results = null;

        Location.distanceBetween(myLatitude, myLongitude, peerLatitude, peerLongitude, results);

        if(results[0] < spaceThreshHold && timeCorelation > timeThreshHold) {
            return true;
        } else {
            return false;
        }

    }
}
