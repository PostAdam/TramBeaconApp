package com.pp.beacon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.RegionUtils;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // region Privates

    private BeaconManager beaconManager;
    private BeaconRegion region;
    public static ListView beaconList;
    private AppCompatActivity appCompatActivity = this;
    public static EditText usernameText;

    // endregion

    // region API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconList = findViewById(R.id.beaconsList);
        beaconManager = new BeaconManager(this);
        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                ListViewAdapter adapter
                        = new ListViewAdapter(appCompatActivity, generateBeaconsDataList(list));
                beaconList.setAdapter(adapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    public void switchActivity(View view) {
        startActivity(new Intent(this, SensorsActivity.class));
    }

    // endregion

    // region Auxiliary Methods

    private ArrayList<BeaconData> generateBeaconsDataList(List<Beacon> beacons) {
        ArrayList<BeaconData> beaconsData = new ArrayList<BeaconData>();

        for (Beacon beacon : beacons) {
            String beaconId = beacon.getUniqueKey();
            String beaconProximity = String.valueOf(RegionUtils.computeAccuracy(beacon));
            beaconsData.add(new BeaconData(beaconId, beaconProximity));
        }

        return beaconsData;
    }

    // endregion
}
