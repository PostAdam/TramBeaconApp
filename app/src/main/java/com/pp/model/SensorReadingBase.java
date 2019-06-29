package com.pp.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Getter;

@Getter
public class SensorReadingBase implements Serializable {
    protected String NearestBeaconId;
    private Timestamp timeStamp;
    protected double aX;
    protected double aY;
    protected double aZ;
    protected String accelerometerUnit = "m/s^2";
    protected double gX;
    protected double gY;
    protected double gZ;
    protected String GyroscopeUnit = "rad/s";
    protected double latitude;
    protected double longitude;
    protected double vehicleLatitude;
    protected double vehicleLongitude;
    protected String locationUnit = "dd";

    public void setNearestBeaconId(String nearestBeaconId) {
        NearestBeaconId = nearestBeaconId;
    }

    public void setaX(double aX) {
        this.aX = formatDouble(aX);
    }

    public void setaY(double aY) {
        this.aY = formatDouble(aY);
    }

    public void setaZ(double aZ) {
        this.aZ = formatDouble(aZ);
    }

    public void setAccelerometerValues(float[] accelerometerValues) {
        this.aX = accelerometerValues[0];
        this.aY = accelerometerValues[1];
        this.aZ = accelerometerValues[2];
    }

    public void setAccelerometerUnit(String accelerometerUnit) {
        this.accelerometerUnit = accelerometerUnit;
    }

    public void setgX(double gX) {
        this.gX = formatDouble(gX);
    }

    public void setgY(double gY) {
        this.gY = formatDouble(gY);
    }

    public void setgZ(double gZ) {
        this.gZ = formatDouble(gZ);
    }

    public void setGyroscopeValues(float[] gyroscopeValues) {
        this.gX = gyroscopeValues[0];
        this.gY = gyroscopeValues[1];
        this.gZ = gyroscopeValues[2];
    }

    public void setGyroscopeUnit(String gyroscopeUnit) {
        GyroscopeUnit = gyroscopeUnit;
    }

    public void setLatitude(double latitude) {
        this.latitude = formatDouble(latitude);
    }

    public void setLongitude(double longitude) {
        this.longitude = formatDouble(longitude);
    }

    public void setVehicleLatitude(double vehicleLatitude) {
        this.vehicleLatitude = formatDouble(vehicleLatitude);
    }

    public void setVehicleLongitude(double vehicleLongitude) {
        this.vehicleLongitude = formatDouble(vehicleLongitude);
    }

    public void setLocationUnit(String locationUnit) {
        this.locationUnit = locationUnit;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    protected double formatDouble(double number) {
        String formattedString = Double.toString(number).replace(",", ".");
        return Double.valueOf(formattedString);
    }
}
