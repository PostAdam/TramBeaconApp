package com.pp.beacon;

import android.content.Context;
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
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SensorsActivity extends AppCompatActivity implements SensorEventListener {
    // region Privates

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private double ax, ay, az;
    private double gx, gy, gz;
    private double longitude, latitude;
    private final String url = "itram.azurewebsites.net/api/sensorreadings/new";

    // endregion

    // region API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        setUpBackButton();
        setUpSensors();
        setUpLocalization();

        if (isNetworkConnectionEnabled()) {
            callAsynchronousTask();
        }
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
            TextView a = findViewById(R.id.ax);
            a.setText(String.valueOf(ax));
            a = findViewById(R.id.ay);
            a.setText(String.valueOf(ay));
            a = findViewById(R.id.az);
            a.setText(String.valueOf(az));
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];
            TextView g = findViewById(R.id.gx);
            g.setText(String.valueOf(gx));
            g = findViewById(R.id.gy);
            g.setText(String.valueOf(gy));
            g = findViewById(R.id.gz);
            g.setText(String.valueOf(gz));
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
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            HTTPAsyncTask task = new HTTPAsyncTask();
                            task.execute(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }

    private void setUpBackButton() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
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

    // endregion

    // region Private Classes

    private class PhoneLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();

            TextView dimension = findViewById(R.id.latitude);
            dimension.setText(String.valueOf(String.valueOf(latitude)));
            dimension = findViewById(R.id.longitude);
            dimension.setText(String.valueOf(String.valueOf(longitude)));
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
        @Override
        protected String doInBackground(String... urls) {
            try {
                try {
                    return HttpPost(urls[0]);
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

        private JSONObject buildJsonObject() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ax", ax);
            jsonObject.put("ay", ay);
            jsonObject.put("az", az);
            jsonObject.put("gx", gx);
            jsonObject.put("gy", gy);
            jsonObject.put("gz", gz);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);

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

        @Override
        protected void onPostExecute(String result) {
        }
    }

    // endregion
}