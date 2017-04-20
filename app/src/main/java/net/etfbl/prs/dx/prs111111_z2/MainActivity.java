package net.etfbl.prs.dx.prs111111_z2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/****************************************************************************
 *
 * Copyright (c) 2017 Elektrotehnički fakultet
 * Patre 5, Banja Luka
 *
 * All Rights Reserved
 *
 * \file MainActivity.java
 * \brief
 *      This class is used for everything.
 *
 * Created on 20.04.2017.
 *
 * @Author Dragan Milanović
 *
 * \notes
 *      20.04.2017. -  logs and checks removed
 *
 * \history
 *
 **********************************************************************/

public class MainActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback{

    private TextView textDirection;
    // For orientation
    private SensorManager mSensorManager;
    private Sensor mSensor;
    // For the camera live feed
    private SurfaceView SurView;
    private SurfaceHolder camHolder;
    private boolean previewRunning;
    public static Camera camera = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // keep screen light on (no lock screen)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        textDirection = (TextView) findViewById(R.id.text);

        // Check if the phone has a camera
        // (popup for camera permission request is not implemented)
        if(checkCameraHardware(this)){
            // Set the background
            SurView = (SurfaceView)findViewById(R.id.surfaceView);
            camHolder = SurView.getHolder();
            camHolder.addCallback(this);
            camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }else{
            // No camera no background
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_camera_warning), Toast.LENGTH_SHORT).show();
        }
    }

    ///////////////////////////////// FOR ENERGY SAVING /////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mSensor);
    }
    ///////////////////////////////// FOR ENERGY SAVING /////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera=Camera.open();
            camera.setDisplayOrientation(90); // Because portrait mode
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.camera_bug), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(previewRunning){
                camera.stopPreview();
            }
            try{
                Camera.Parameters camParams = camera.getParameters();
                Camera.Size size = camParams.getSupportedPreviewSizes().get(0);
                camParams.setPreviewSize(size.width, size.height);
                camera.setParameters(camParams);

                camera.setPreviewDisplay(holder);
                camera.startPreview();
                previewRunning=true;
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.camera_bug), Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera=null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            updateTextDirection(Math.round(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No actions
    }

    /************************************************************************/
    /**
     *
     *  @brief   Uses the sensor value and displays it.
     *
     *  @param   bearing - the angle in degrees
     *
     ************************************************************************/
    private void updateTextDirection(double bearing) {
        /* Text direction
        int range = (int) (bearing / (360f / 16f));
        String dirTxt = "";
        if (range == 15 || range == 0) dirTxt = "N";
        if (range == 1 || range == 2) dirTxt = "NE";
        if (range == 3 || range == 4) dirTxt = "E";
        if (range == 5 || range == 6) dirTxt = "SE";
        if (range == 7 || range == 8) dirTxt = "S";
        if (range == 9 || range == 10) dirTxt = "SW";
        if (range == 11 || range == 12) dirTxt = "W";
        if (range == 13 || range == 14) dirTxt = "NW";
        textDirection.setText("" + ((int) bearing) + ((char) 176) + " " + dirTxt);  // (char)176 = degrees ...
        */
        textDirection.setText("" + ((int) bearing));
    }

    /************************************************************************/
    /**
     *
     *  @brief   Check if this device has a camera.
     *
     *
     ************************************************************************/
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;  // this device has a camera
        } else {
            return false; // no camera on this device
        }
    }

}
