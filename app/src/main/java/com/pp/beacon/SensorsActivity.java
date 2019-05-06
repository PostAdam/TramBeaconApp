package com.pp.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SensorsActivity extends AppCompatActivity implements SensorEventListener {
    // region Privates

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private BroadcastReceiver broadcastReceiver;

    private double ax, ay, az;
    private double gx, gy, gz;
    private float numberOfSteps;
    private float stepDetector;
    private double gravityX, gravityY, gravityZ;
    private double rotationVecX, rotationVecY, rotationVecZ;

    private double light;
    private double pressure;

    private double gameRotationVecX, gameRotationVecY, gameRotationVecZ;
    private double geomagneticRotationVecX, geomagneticRotationVecY, geomagneticRotationVecZ;
    private double magneticFieldX, magneticFieldY, magneticFieldZ;
    private double proximity;

    private double longitude, latitude;
    private int batteryLevel;

    private Switch simpleSwitch;
    private boolean imInTram;

    private String username;
    private BeaconData beaconData;

    private int postCounter = 0;
    private TextView postCounterTextView;
    private final String url = "http://itram.azurewebsites.net/api/sensorreadings/multiple-new";
    //private final String url = "http://itram.azurewebsites.net/api/sensorreadings/new";
    // endregion

    // region API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        postCounterTextView = findViewById(R.id.postCounter);

        setUpSwitch();
        setUpBackButton();
        setUpSensors();
        setUpLocalization();
        setUpBattery();

        username = MainActivity.usernameText.getText().toString();
        try {
            beaconData = (BeaconData) MainActivity.beaconList.getAdapter().getItem(0);
        } catch (Exception e) {
            System.out.print(e.getMessage());
            beaconData = new BeaconData("No Beacon here", "");
        }

        if (isNetworkConnectionEnabled()) {
            callAsynchronousTask();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            numberOfSteps = event.values[0];
        }

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gravityX = event.values[0];
            gravityY = event.values[1];
            gravityZ = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            rotationVecX = event.values[0];
            rotationVecY = event.values[1];
            rotationVecZ = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepDetector = event.values[0];
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            light = event.values[0];
        }

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressure = event.values[0];
        }

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            gameRotationVecX = event.values[0];
            gameRotationVecY = event.values[1];
            gameRotationVecZ = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            geomagneticRotationVecX = event.values[0];
            geomagneticRotationVecY = event.values[1];
            geomagneticRotationVecZ = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldX = event.values[0];
            magneticFieldY = event.values[1];
            magneticFieldZ = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proximity = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // endregion

    // region Auxiliary Methods

    private boolean isNetworkConnectionEnabled() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        final ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            HTTPAsyncTask task = new HTTPAsyncTask(jsonObjects);
                            task.execute(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000); //execute in every 5000 ms
    }

    private void setUpBackButton() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setUpLocalization() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, new PhoneLocationListener());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpBattery() {
        broadcastReceiver = new BatteryBroadcastReceiver();
    }

    private void setUpSwitch() {
        simpleSwitch = findViewById(R.id.simpleSwitch);
        imInTram = simpleSwitch.isChecked();
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                imInTram = isChecked;
            }
        });
    }

    // endregion

    // region Private Classes

    private class PhoneLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        public ArrayList<JSONObject> jsonObjects;

        public HTTPAsyncTask(ArrayList<JSONObject> jsonList) {
            jsonObjects = jsonList;
        }

        @Override
        protected void onPreExecute() {
            postCounterTextView.setText(String.valueOf(++postCounter));
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                try {
                    return HttpPostPackage(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        private String HttpPost(String myUrl) throws IOException, JSONException {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            JSONObject jsonObject = buildJsonObject();
            setPostRequestContent(conn, jsonObject);
            conn.connect();

            return conn.getResponseMessage();
        }

        private String HttpPostPackage(String myUrl) throws IOException, JSONException {

            if(this.jsonObjects.size() < 10) {
                this.jsonObjects.add(buildJsonObject());
                System.out.println("Adding JSON object to list (" + jsonObjects.size() + ")");
                return "Adding JSON object to list";
            } else {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                System.out.println("Sending JSON package");
                setPostRequestPackageContent(conn, this.jsonObjects);
                System.out.println("JSON package sent");
                conn.connect();

                this.jsonObjects.clear();

                return conn.getResponseMessage();
            }

        }

        private double formatDouble(double number) {
            String formattedString = String.format(Locale.US, "%.3f", number);
            return Double.valueOf(formattedString);
        }

        private JSONObject buildJsonObject() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Username", username);
            jsonObject.put("CurrentDate", Calendar.getInstance().getTime());
            jsonObject.put("NearestBeaconId", beaconData.getBeaconId());
            jsonObject.put("Ax", formatDouble(ax));
            jsonObject.put("Ay", formatDouble(ay));
            jsonObject.put("Az", formatDouble(az));
            jsonObject.put("AccelerometerUnit", "m/s^2");
            jsonObject.put("Gx", formatDouble(gx));
            jsonObject.put("Gy", formatDouble(gy));
            jsonObject.put("Gz", formatDouble(gz));
            jsonObject.put("GyroscopeUnit", "rad/s");
            jsonObject.put("Latitude", latitude);
            jsonObject.put("Longitude", longitude);
            jsonObject.put("LocationUnit", "dd");
            jsonObject.put("BatteryLevel", batteryLevel);
            jsonObject.put("NumberOfSteps", numberOfSteps);
            jsonObject.put("StepDetector", stepDetector);
            jsonObject.put("GravityX", formatDouble(gravityX));
            jsonObject.put("GravityY", formatDouble(gravityY));
            jsonObject.put("GravityZ", formatDouble(gravityZ));
            jsonObject.put("RotationVecX", formatDouble(rotationVecX));
            jsonObject.put("RotationVecY", formatDouble(rotationVecY));
            jsonObject.put("RotationVecZ", formatDouble(rotationVecZ));
            jsonObject.put("Light", formatDouble(light));
            jsonObject.put("Pressure", formatDouble(pressure));
            jsonObject.put("GameRotationVecX", formatDouble(gameRotationVecX));
            jsonObject.put("GameRotationVecY", formatDouble(gameRotationVecY));
            jsonObject.put("GameRotationVecZ", formatDouble(gameRotationVecZ));
            jsonObject.put("GeomagneticRotationVecX", formatDouble(geomagneticRotationVecX));
            jsonObject.put("GeomagneticRotationVecY", formatDouble(geomagneticRotationVecY));
            jsonObject.put("GeomagneticRotationVecZ", formatDouble(geomagneticRotationVecZ));
            jsonObject.put("MagneticFieldX", formatDouble(magneticFieldX));
            jsonObject.put("MagneticFieldY", formatDouble(magneticFieldY));
            jsonObject.put("MagneticFieldZ", formatDouble(magneticFieldZ));
            jsonObject.put("Proximity", formatDouble(proximity));
            jsonObject.put("ImInTram", imInTram);



            return jsonObject;
        }

        private void setPostRequestContent(HttpURLConnection conn,
                                           JSONObject jsonObject) throws IOException {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();
            os.close();
        }

        private void setPostRequestPackageContent(HttpURLConnection conn,
                                           ArrayList<JSONObject> jsonObjects) throws IOException {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            JSONArray jsonArray = new JSONArray();
            for(JSONObject jsonObject : jsonObjects) {
                System.out.println("Adding single JSON object to buffor");
                jsonArray.put(jsonObject);
            }

            writer.write(jsonArray.toString());
            writer.flush();
            writer.close();
            os.close();
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        private final static String BATTERY_LEVEL = "level";

        @Override
        public void onReceive(Context context, Intent intent) {
            batteryLevel = intent.getIntExtra(BATTERY_LEVEL, 0);
        }
    }

    // endregion
}