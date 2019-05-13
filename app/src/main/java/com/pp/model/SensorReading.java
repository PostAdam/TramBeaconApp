package com.pp.model;

import java.util.Locale;

public class SensorReading {
    private String NearestBeaconId;
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

    public String getNearestBeaconId() {
        return NearestBeaconId;
    }

    public void setNearestBeaconId(String nearestBeaconId) {
        NearestBeaconId = nearestBeaconId;
    }

    public double getaX() {
        return aX;
    }

    public void setaX(double aX) {
        this.aX = formatDouble(aX);
    }

    public double getaY() {
        return aY;
    }

    public void setaY(double aY) {
        this.aY = formatDouble(aY);
    }

    public double getaZ() {
        return aZ;
    }

    public void setaZ(double aZ) {
        this.aZ = formatDouble(aZ);
    }

    public String getAccelerometerUnit() {
        return accelerometerUnit;
    }

    public void setAccelerometerUnit(String accelerometerUnit) {
        this.accelerometerUnit = accelerometerUnit;
    }

    public double getgX() {
        return gX;
    }

    public void setgX(double gX) {
        this.gX = formatDouble(gX);
    }

    public double getgY() {
        return gY;
    }

    public void setgY(double gY) {
        this.gY = formatDouble(gY);
    }

    public double getgZ() {
        return gZ;
    }

    public void setgZ(double gZ) {
        this.gZ = formatDouble(gZ);
    }

    public String getGyroscopeUnit() {
        return GyroscopeUnit;
    }

    public void setGyroscopeUnit(String gyroscopeUnit) {
        GyroscopeUnit = gyroscopeUnit;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = formatDouble(latitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = formatDouble(longitude);
    }

    public String getLocationUnit() {
        return locationUnit;
    }

    public void setLocationUnit(String locationUnit) {
        this.locationUnit = locationUnit;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public float getNumberOfSteps() {
        return numberOfSteps;
    }

    public void setNumberOfSteps(float numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    public float getStepDetector() {
        return stepDetector;
    }

    public void setStepDetector(float stepDetector) {
        this.stepDetector = stepDetector;
    }

    public double getGravityX() {
        return gravityX;
    }

    public void setGravityX(double gravityX) {
        this.gravityX = formatDouble(gravityX);
    }

    public double getGravityY() {
        return gravityY;
    }

    public void setGravityY(double gravityY) {
        this.gravityY = formatDouble(gravityY);
    }

    public double getGravityZ() {
        return gravityZ;
    }

    public void setGravityZ(double gravityZ) {
        this.gravityZ = formatDouble(gravityZ);
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public void setRotationZ(double rotationZ) {
        this.rotationZ = formatDouble(rotationZ);
    }

    public double getRotationY() {
        return rotationY;
    }

    public void setRotationY(double rotationY) {
        this.rotationY = formatDouble(rotationY);
    }

    public double getRotationX() {
        return rotationX;
    }

    public void setRotationX(double rotationX) {
        this.rotationX = formatDouble(rotationX);
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = formatDouble(light);
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = formatDouble(pressure);
    }

    public double getGameRotationX() {
        return gameRotationX;
    }

    public void setGameRotationX(double gameRotationX) {
        this.gameRotationX = formatDouble(gameRotationX);
    }

    public double getGameRotationY() {
        return gameRotationY;
    }

    public void setGameRotationY(double gameRotationY) {
        this.gameRotationY = formatDouble(gameRotationY);
    }

    public double getGameRotationZ() {
        return gameRotationZ;
    }

    public void setGameRotationZ(double gameRotationZ) {
        this.gameRotationZ = formatDouble(gameRotationZ);
    }

    public double getGeomagneticRotationX() {
        return geomagneticRotationX;
    }

    public void setGeomagneticRotationX(double geomagneticRotationX) {
        this.geomagneticRotationX = formatDouble(geomagneticRotationX);
    }

    public double getGeomagneticRotationY() {
        return geomagneticRotationY;
    }

    public void setGeomagneticRotationY(double geomagneticRotationY) {
        this.geomagneticRotationY = formatDouble(geomagneticRotationY);
    }

    public double getGeomagneticRotationZ() {
        return geomagneticRotationZ;
    }

    public void setGeomagneticRotationZ(double geomagneticRotationZ) {
        this.geomagneticRotationZ = formatDouble(geomagneticRotationZ);
    }

    public double getMagneticFieldX() {
        return magneticFieldX;
    }

    public void setMagneticFieldX(double magneticFieldX) {
        this.magneticFieldX = formatDouble(magneticFieldX);
    }

    public double getMagneticFieldY() {
        return magneticFieldY;
    }

    public void setMagneticFieldY(double magneticFieldY) {
        this.magneticFieldY = formatDouble(magneticFieldY);
    }

    public double getMagneticFieldZ() {
        return magneticFieldZ;
    }

    public void setMagneticFieldZ(double magneticFieldZ) {
        this.magneticFieldZ = formatDouble(magneticFieldZ);
    }

    public double getProximity() {
        return proximity;
    }

    public void setProximity(double proximity) {

        this.proximity = formatDouble(proximity);
    }

    public boolean isImInTram() {
        return imInTram;
    }

    public void setImInTram(boolean imInTram) {
        this.imInTram = imInTram;
    }

    private double formatDouble(double number) {
        String formattedString = String.format(Locale.US, "%.3f", number);
        return Double.valueOf(formattedString);
    }
}
