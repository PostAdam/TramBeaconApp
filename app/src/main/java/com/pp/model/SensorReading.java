package com.pp.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Getter;

@Getter
public class SensorReading implements Serializable {
    private String NearestBeaconId;
    private Timestamp timeStamp;
    private double aX;
    private double aY;
    private double aZ;
    private String accelerometerUnit;
    private double gX;
    private double gY;
    private double gZ;
    private String GyroscopeUnit;
    private double latitude;
    private double longitude;
    private String locationUnit;
    private int batteryLevel;
    private float numberOfSteps;
    private float stepDetector;
    private double gravityX;
    private double gravityY;
    private double gravityZ;
    private double rotationZ;
    private double rotationY;
    private double rotationX;
    private double light;
    private double pressure;
    private double gameRotationX;
    private double gameRotationY;
    private double gameRotationZ;
    private double geomagneticRotationX;
    private double geomagneticRotationY;
    private double geomagneticRotationZ;
    private double magneticFieldX;
    private double magneticFieldY;
    private double magneticFieldZ;
    private double proximity;
    private boolean imInTram;

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

    public void setGyroscopeUnit(String gyroscopeUnit) {
        GyroscopeUnit = gyroscopeUnit;
    }

    public void setLatitude(double latitude) {
        this.latitude = formatDouble(latitude);
    }

    public void setLongitude(double longitude) {
        this.longitude = formatDouble(longitude);
    }

    public void setLocationUnit(String locationUnit) {
        this.locationUnit = locationUnit;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setNumberOfSteps(float numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    public void setStepDetector(float stepDetector) {
        this.stepDetector = stepDetector;
    }

    public void setGravityX(double gravityX) {
        this.gravityX = formatDouble(gravityX);
    }

    public void setGravityY(double gravityY) {
        this.gravityY = formatDouble(gravityY);
    }

    public void setGravityZ(double gravityZ) {
        this.gravityZ = formatDouble(gravityZ);
    }

    public void setRotationZ(double rotationZ) {
        this.rotationZ = formatDouble(rotationZ);
    }

    public void setRotationY(double rotationY) {
        this.rotationY = formatDouble(rotationY);
    }

    public void setRotationX(double rotationX) {
        this.rotationX = formatDouble(rotationX);
    }

    public void setLight(double light) {
        this.light = formatDouble(light);
    }

    public void setPressure(double pressure) {
        this.pressure = formatDouble(pressure);
    }

    public void setGameRotationX(double gameRotationX) {
        this.gameRotationX = formatDouble(gameRotationX);
    }

    public void setGameRotationY(double gameRotationY) {
        this.gameRotationY = formatDouble(gameRotationY);
    }

    public void setGameRotationZ(double gameRotationZ) {
        this.gameRotationZ = formatDouble(gameRotationZ);
    }

    public void setGeomagneticRotationX(double geomagneticRotationX) {
        this.geomagneticRotationX = formatDouble(geomagneticRotationX);
    }

    public void setGeomagneticRotationY(double geomagneticRotationY) {
        this.geomagneticRotationY = formatDouble(geomagneticRotationY);
    }

    public void setGeomagneticRotationZ(double geomagneticRotationZ) {
        this.geomagneticRotationZ = formatDouble(geomagneticRotationZ);
    }

    public void setMagneticFieldX(double magneticFieldX) {
        this.magneticFieldX = formatDouble(magneticFieldX);
    }

    public void setMagneticFieldY(double magneticFieldY) {
        this.magneticFieldY = formatDouble(magneticFieldY);
    }

    public void setMagneticFieldZ(double magneticFieldZ) {
        this.magneticFieldZ = formatDouble(magneticFieldZ);
    }

    public void setProximity(double proximity) {
        this.proximity = formatDouble(proximity);
    }

    public void setImInTram(boolean imInTram) {
        this.imInTram = imInTram;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    private double formatDouble(double number) {
        String formattedString = Double.toString(number).replace(",", ".");
        return Double.valueOf(formattedString);
    }
}
