package com.nkyrim.autolight;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nkyrim.autolight.flashlight.Flashlight;
import com.nkyrim.autolight.flashlight.FlashlightException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // UI elements
    private TextView tvCurrentLux;
    private TextView tvDefinedLux;
    private SeekBar skDefLux;
    // Fields
    private float lux = 0;
    private SensorManager sm;
    private Flashlight flashlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Light Sensor
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);

        // if no light sensor show error message and return else register it
        if (lightSensor == null && savedInstanceState == null) {
            Toast.makeText(this, R.string.no_sensor_error, Toast.LENGTH_LONG).show();
            return;
        }
        sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Initialize ui elements
        tvCurrentLux = findViewById(R.id.tvCurrentLux);
        tvDefinedLux = findViewById(R.id.tvDefinedLux);
        skDefLux = findViewById(R.id.skDefLux);
        skDefLux.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDefinedLux.setText(String.valueOf(progress));
                triggerLight();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        // prevent the device from going to standby
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // check flash availability
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            // no flash show message
            Toast.makeText(this, R.string.no_flash_error, Toast.LENGTH_LONG).show();
        } else {
            // Initialize camera
            flashlight = Flashlight.create(this);
            if (flashlight == null)
                Toast.makeText(this, R.string.no_camera_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(this);
        if (flashlight != null) {
            try {
                flashlight.open();
            } catch (FlashlightException exc) {
                Toast.makeText(this, R.string.flashlight_close_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get current lux value
        lux = event.values[0];

        // Update current value TextView
        tvCurrentLux.setText(String.valueOf(lux));

        // trigger light
        triggerLight();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    /**
     * Turns light on or off according to the current lux value
     */
    private void triggerLight() {
        // light the flash if below defined threshold
        if (lux < skDefLux.getProgress()) {
            if (flashlight == null) {
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 1F;
                getWindow().setAttributes(layout);
            } else {
                try {
                    flashlight.open();
                } catch (FlashlightException exc) {
                    Toast.makeText(this, R.string.flashlight_open_error, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            if (flashlight == null) {
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 0F;
                getWindow().setAttributes(layout);
            } else {
                try {
                    flashlight.open();
                } catch (FlashlightException exc) {
                    Toast.makeText(this, R.string.flashlight_close_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
