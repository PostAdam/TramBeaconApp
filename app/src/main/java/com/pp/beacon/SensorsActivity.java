package com.pp.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pp.model.SensorReading;
import com.pp.retrofit.RetrofitClientInstance;
import com.pp.retrofit.SensorsReadingService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorsActivity extends AppCompatActivity implements SensorEventListener {
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

    private ArrayList<SensorReading> readingsJsonArray = new ArrayList<SensorReading>();

    private static final String JWT_PREFERENCES = "jwtPreferences";
    private static final String USERID_FIELD = "UserId";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        preferences = getSharedPreferences(JWT_PREFERENCES, MODE_PRIVATE);

        postCounterTextView = findViewById(R.id.postCounter);

        setUpSwitch();
        setUpBackButton();
        setUpSensors();
        setUpLocalization();
        setUpBattery();

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
                            SensorsActivity.HTTPAsyncTask task = new SensorsActivity.HTTPAsyncTask();
                            task.execute();
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, new SensorsActivity.PhoneLocationListener());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpBattery() {
        broadcastReceiver = new SensorsActivity.BatteryBroadcastReceiver();
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

    private class HTTPAsyncTask extends AsyncTask<Void, Void, Void> {
        public ArrayList<JSONObject> jsonObjects;

        public HTTPAsyncTask() {
        }//ArrayList<JSONObject> jsonList) {
        //  jsonObjects = jsonList;
        //}

        @Override
        protected void onPreExecute() {
            postCounterTextView.setText(String.valueOf(++postCounter));
        }

        @Override
        protected Void doInBackground(Void... voids) {

            HttpPostPackage();

            return null;
        }
    }

    private void HttpPostPackage() {

        if(this.readingsJsonArray.size() < 10) {
            this.readingsJsonArray.add(buildSensorReading());

        } else {
            String json = new Gson().toJson(readingsJsonArray);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            SensorsReadingService service = RetrofitClientInstance.getRetrofitInstance().create(SensorsReadingService.class);
            Call<Void> call = service.postMultipleReadings(body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    System.out.println("###CODE: "+response.code());
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });

            this.readingsJsonArray.clear();
        }

    }

    private SensorReading buildSensorReading() {
        String userId = preferences.getString(USERID_FIELD, "");
        SensorReading reading = new SensorReading();
        reading.setUserId(Integer.parseInt(userId));
        reading.setNearestBeaconId(beaconData.getBeaconId());
        reading.setaX(ax);
        reading.setaY(ay);
        reading.setaZ(az);
        reading.setAccelerometerUnit("m/s^2");
        reading.setgX(gx);
        reading.setgY(gy);
        reading.setgZ(gz);
        reading.setGyroscopeUnit("rad/s");
        reading.setLatitude(latitude);
        reading.setLongitude(longitude);
        reading.setLocationUnit("dd");
        reading.setBatteryLevel(batteryLevel);
        reading.setNumberOfSteps(numberOfSteps);
        reading.setStepDetector(stepDetector);
        reading.setGravityX(gravityX);
        reading.setGravityY(gravityY);
        reading.setGravityZ(gravityZ);
        reading.setRotationX(rotationVecX);
        reading.setRotationY(rotationVecY);
        reading.setRotationZ(rotationVecZ);
        reading.setLight(light);
        reading.setPressure(pressure);
        reading.setGameRotationX(gameRotationVecX);
        reading.setGameRotationY(gameRotationVecY);
        reading.setGameRotationZ(gameRotationVecZ);
        reading.setGeomagneticRotationX(geomagneticRotationVecX);
        reading.setGeomagneticRotationY(geomagneticRotationVecY);
        reading.setGeomagneticRotationZ(geomagneticRotationVecZ);
        reading.setMagneticFieldX(magneticFieldX);
        reading.setMagneticFieldY(magneticFieldY);
        reading.setMagneticFieldZ(magneticFieldZ);
        reading.setProximity(proximity);
        reading.setInTram(imInTram);

        return reading;
    }

    private double formatDouble(double number) {
        String formattedString = String.format(Locale.US, "%.3f", number);
        return Double.valueOf(formattedString);
    }

    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        private final static String BATTERY_LEVEL = "level";

        @Override
        public void onReceive(Context context, Intent intent) {
            batteryLevel = intent.getIntExtra(BATTERY_LEVEL, 0);
        }
    }
}
