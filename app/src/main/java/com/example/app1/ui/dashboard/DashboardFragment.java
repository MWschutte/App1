package com.example.app1.ui.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app1.databinding.FragmentDashboardBinding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    Button buttonRssi0;
    Button buttonRssi1;
    Button buttonRssi2;
    Button buttonRssi3;
    Button buttonRssi4;
    Button buttonRssi5;
    Button buttonRssi6;
    Button buttonRssi7;

    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    /**
     * The text view.
     */
    private TextView textRssi;
    /**
     * The button.
     */
    List<ScanResult> oldScanResult = new ArrayList<>();

    int times = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        buttonRssi0 = binding.buttonRSSI0;
        buttonRssi1 = binding.buttonRSSI1;
        buttonRssi2 = binding.buttonRSSI2;
        buttonRssi3 = binding.buttonRSSI3;
        buttonRssi4 = binding.buttonRSSI4;
        buttonRssi5 = binding.buttonRSSI5;
        buttonRssi6 = binding.buttonRSSI6;
        buttonRssi7 = binding.buttonRSSI7;

        List<Button> buttons = Arrays.asList(buttonRssi0, buttonRssi1, buttonRssi2, buttonRssi3, buttonRssi4, buttonRssi5, buttonRssi6,
        buttonRssi7);

        textRssi = binding.textRSSI;

        for(int i=0; i<buttons.size(); i++) {
            Button button = buttons.get(i);
            int finalI = i;

            button.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    // Set text.


                    textRssi.setText("\n\tScan all access points: " + String.valueOf(times) );
                    times += 1;
                    // Set wifi manager.
                    wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    // Start a wifi scan.
                    wifiManager.startScan();
                    // Store results in a list.
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    // Write results to a label
                    for (ScanResult scanResult : scanResults) {
                        textRssi.setText(textRssi.getText() + "\n\tBSSID = "
                                + scanResult.BSSID + "    RSSI = "
                                + scanResult.level + "dBm");
                    }
                    if(!oldEqualsNew(scanResults)){
                        textRssi.setText("new RSSI scan \n" + textRssi.getText());
                        writeScanToFile(scanResults, finalI);
                    } else {
                        textRssi.setText("old RSSI scan \n" + textRssi.getText());
                    }




                    oldScanResult = new ArrayList<>(scanResults);
                }
            });
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public boolean oldEqualsNew(List<ScanResult> scanResults) {
        if(oldScanResult.size()!= scanResults.size()) {
            return false;
        }
        for(int i=0; i<oldScanResult.size(); i++) {
            ScanResult oldScan = oldScanResult.get(i);
            ScanResult newScan = scanResults.get(i);
            if(!oldScan.BSSID.equals( newScan.BSSID )|| oldScan.level != newScan.level) {
                return false;
            }
        }
        return true;
    }

    public void writeScanToFile(List<ScanResult> scanResults, int cell) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(" yyyy-MM-dd HH.mm.ss");

        try {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            String filename = "cell-"+String.valueOf(cell) + dateFormat.format(timestamp);
            String content = writeCsvContent(scanResults);


            //File f =  getContext().getFilesDir();
            File folder_new = new File(folder, "RSSI_data");
            folder_new.mkdir();
            File file = new File(folder_new, filename+".csv");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String writeCsvContent(List<ScanResult> scanResults) {
        String res = "";
        for(int i=0; i<scanResults.size(); i++) {
            ScanResult value = scanResults.get(i);
            res += value.BSSID + "," + String.valueOf(value.level) + "\n";

        }
        return res;
    }
}