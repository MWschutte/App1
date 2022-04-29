package com.example.app1.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app1.databinding.FragmentDashboardBinding;

import java.lang.reflect.Array;
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
}