package com.pp.beacon;

public class BeaconData {
    private String beaconId;
    private String beaconProximity;

    public BeaconData(String beaconId, String beaconProximity) {
        this.beaconId = beaconId;
        this.beaconProximity = beaconProximity;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public String getBeaconProximity() {
        return beaconProximity;
    }
}
