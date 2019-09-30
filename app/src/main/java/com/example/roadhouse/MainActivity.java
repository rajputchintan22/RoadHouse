package com.example.roadhouse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    Button record;
    EditText intervalInput;
    String interval;

    Handler handler;
    Runnable runnable;

    FileWriter writer;
    BufferedWriter bw;
    PrintWriter pw;

    String output;
    File root;


    LocationManager locationManager;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        record=findViewById(R.id.Record);
        intervalInput=findViewById(R.id.Interval);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date currentTime= Calendar.getInstance().getTime();

                String time=currentTime.toString();
                interval=intervalInput.getText().toString();
                if(interval.length()==0){
                    interval="1000";
                }

                /*locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);*/


                final Uri textFile=getOutputMediaFile(69);
                root = new File(textFile.getPath());

                handler=new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //Log.d("chintan","location capture here");
                           Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            if (mLastLocation != null) {
                               String mLatitudeText=String.valueOf(mLastLocation.getLatitude());
                               String mLongitudeText=String.valueOf(mLastLocation.getLongitude());
                               Log.d("chintan","lat="+mLatitudeText+" long="+mLongitudeText);

                               output+="\n"+"lat="+mLatitudeText+" long="+mLongitudeText;


                            }

                        }
                        catch (Exception e) {
                            // TODO: handle exception
                        }
                        finally{
                            //also call the same runnable to call it at regular interval
                            handler.postDelayed(this, Integer.parseInt(interval));

                        }
                    }
                };

                handler.post(runnable);


                Intent takeVideoIntent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,50);
                Uri fileUri = getOutputMediaFile(MEDIA_TYPE_VIDEO);


                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                startActivityForResult(takeVideoIntent,1);
            }
        });

    }

    public Uri getOutputMediaFile(int type)
    {
        // To be safe, you should check that the SDCard is mounted

        if(Environment.getExternalStorageState() != null) {
            // this works for Android 2.2 and above
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory("Road_House"), "SMW_VIDEO");

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()) {
                if (! mediaStorageDir.mkdirs()) {
                    Log.d("chintan", "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            if(type == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(mediaStorageDir.getPath()+File.separator +
                        "VID_"+ timeStamp + ".mp4");
            } else if(type==69){
                mediaFile=new File(mediaStorageDir.getPath()+File.separator +
                        "TXT_"+ timeStamp + ".txt");
            }else {
                return null;
            }

            return Uri.fromFile(mediaFile);
        }

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("chintan","here\noutput="+output);
        handler.removeCallbacks(runnable);
        try{
         FileOutputStream fOut = new FileOutputStream(root);
         OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
         myOutWriter.append(output);
         output="";

         myOutWriter.close();

         fOut.flush();
         fOut.close();}catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("chintan","connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("chintan",connectionResult.getErrorMessage());
    }
}

