package com.example.app1.ui.home;

import static android.content.pm.PackageManager.*;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.app1.databinding.FragmentHomeBinding;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

enum MeasureType {
    WALKING, STANDING, RUNNING
}
class Measure {
    public MeasureType type;
    public String label;

    public Measure(MeasureType type, String label) {
        this.type = type;
        this.label = label;
    }
}

public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    /**
     * The sensor manager object.
     */
    private SensorManager sensorManager;
    /**
     * The accelerometer.
     */
    private Sensor accelerometer;
    /**
     * The wifi info.
     */
    private WifiInfo wifiInfo;
    /**
     * Accelerometer values;
     */
    private float[] acc_data;

    private TextView textView, textAcc;
    private ToggleButton walkButton, standButton, jumpButton;

    private boolean measuring;
    private Measure walking, standing, running;
    private Measure currMeasure = null;
    List<String[]> data = new ArrayList<>();

    // 2021-03-24 16:48:05
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(" yyyy-MM-dd HH.mm.ss");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//
        textView = binding.textHome;
        textAcc = binding.textAcc;
        walkButton = binding.toggleWalking;
        standButton = binding.toggleStanding;
        jumpButton = binding.toggleRunning;
        walking = new Measure(MeasureType.WALKING, "Walking");
        standing = new Measure(MeasureType.STANDING, "Standing still");
        running = new Measure(MeasureType.RUNNING, "Running");

        textView.setText("Select an activity");
        this.setClickListener(walkButton, walking);
        this.setClickListener(standButton, standing);
        this.setClickListener(jumpButton, running);

        // Set the sensor manager
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No accelerometer!
        }

        return root;
    }

    public void setClickListener(ToggleButton b, Measure m) {
        b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textView.setText(m.label);
                    currMeasure = m;
                    measuring = true;
                    data = new ArrayList<>();
                } else {
                    textView.setText("Select an activity");
                    textAcc.setText("");
                    measuring = false;
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    String filename = "/LocalRevolutions/";

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);

                    if (file == null || !file.mkdirs()) {
                        System.out.println("Directory not created");
                    }

                    file = new File(file.getAbsolutePath(), currMeasure.label + dateFormat.format(timestamp) + ".csv");

                    textAcc.setText(file.getAbsolutePath());

                    data.add(0, new String[]{"x", "y", "z", "timestamp"});

                    CSVWriter writer = null;
                    try {
                        writer = new CSVWriter(new FileWriter(file));
                        writer.writeAll(data);
                        writer.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        textAcc.setText(e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(this.measuring) {
            // get the the x,y,z values of the accelerometer
            acc_data = Arrays.copyOfRange(event.values, 0, 3);

            textAcc.setText(String.format("x: %f, y:%f, z:%f", acc_data[0], acc_data[1], acc_data[2]));

            data.add(new String[] {String.valueOf(acc_data[0]), String.valueOf(acc_data[1]), String.valueOf(acc_data[2]), String.valueOf(System.currentTimeMillis())});
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do nothing
    }
}