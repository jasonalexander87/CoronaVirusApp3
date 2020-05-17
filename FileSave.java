package com.iasonas.coronavirus;

import android.app.IntentService;
import android.content.Intent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileSave extends IntentService {

    public FileSave() { super("test-service"); }


    @Override
    protected void onHandleIntent(Intent intent) {


        String locData[] = intent.getStringArrayExtra("data");

        try {
            FileOutputStream fOut = openFileOutput("locData.txt", MODE_APPEND);
            OutputStreamWriter sOut = new OutputStreamWriter(fOut);

            for(int i=0; i< locData.length; i++) {
                sOut.write(locData[i]);
                sOut.flush();
            }

            sOut.close();
            fOut.close();

        } catch (FileNotFoundException e) { }
          catch (IOException e) { }

    }
}
