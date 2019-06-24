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
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pp.model.BeaconData;
import com.pp.model.BeaconToken;
import com.pp.model.SensorReading;
import com.pp.retrofit.BeaconTokenService;
import com.pp.retrofit.RetrofitClientInstance;
import com.pp.retrofit.SensorsReadingService;
import com.pp.services.BluetoothService;
import com.pp.services.FileService;

import org.apache.commons.lang3.SerializationUtils;

import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    private SensorReading sensorReading;
    private ArrayList<SensorReading> readingsJsonArray = new ArrayList<>();
    private int postCounter = 0;

    private Switch simpleSwitch;
    private BeaconData beaconData;
    private TextView postCounterTextView;

    private static final String JWT_PREFERENCES = "jwtPreferences";
    private static final String TOKEN_FIELD = "token";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        sensorReading = new SensorReading();
        sensorReading.setAccelerometerUnit("m/s^2");
        sensorReading.setGyroscopeUnit("rad/s");
        sensorReading.setLocationUnit("dd");

        preferences = getSharedPreferences(JWT_PREFERENCES, MODE_PRIVATE);

        postCounterTextView = findViewById(R.id.postCounter);
        postCounterTextView.setText(String.valueOf(postCounter));

        // get beacon token from server
        BeaconTokenService service = RetrofitClientInstance.getRetrofitInstance().create(BeaconTokenService.class);
        String token = "Bearer " + preferences.getString(TOKEN_FIELD, "");
        Call<List<BeaconToken>> call = service.getAllBeaconTokens(token);
        call.enqueue(new Callback<List<BeaconToken>>() {
            @Override
            public void onResponse(Call<List<BeaconToken>> call, Response<List<BeaconToken>> response) {
                List<BeaconToken> beaconTokens = response.body();

                //save token to file
                URI fileUri = FileService.createAndWriteFile(getApplicationContext().getCacheDir(), "beaconTokens", BeaconToken.tokensToString(beaconTokens));
                // get beacon's mac address
                String beaconMacAddress = MainActivity.beacons.get(0).getMacAddress().toStandardString();
                // send token to beacon
                BluetoothService bluetoothService = new BluetoothService(beaconMacAddress, fileUri);
                // get confirmation
                // if failed send again
                // repeat till confirmed
            }

            @Override
            public void onFailure(Call<List<BeaconToken>> call, Throwable t) {
                Toast.makeText(SensorsActivity.this, "Failed to fetch tokens!", Toast.LENGTH_LONG).show();
            }
        });

        setUpSwitch();
        setUpBackButton();
        setUpSensors();
        setUpLocalization();
        setUpBattery();

        try {
            beaconData = (BeaconData) MainActivity.beaconList.getAdapter().getItem(0);
        } catch (Exception ex) {
            Log.e("Beacon", ex.getMessage());
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
        sensorReading.setTimeStamp(new Timestamp(new Date().getTime()));
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorReading.setaX(event.values[0]);
            sensorReading.setaY(event.values[1]);
            sensorReading.setaZ(event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorReading.setgX(event.values[0]);
            sensorReading.setgY(event.values[1]);
            sensorReading.setgZ(event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            sensorReading.setNumberOfSteps(event.values[0]);
        }

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            sensorReading.setGravityX(event.values[0]);
            sensorReading.setGravityY(event.values[1]);
            sensorReading.setGravityZ(event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            sensorReading.setRotationX(event.values[0]);
            sensorReading.setRotationY(event.values[1]);
            sensorReading.setRotationZ(event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            sensorReading.setStepDetector(event.values[0]);
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            sensorReading.setLight(event.values[0]);
        }

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            sensorReading.setPressure(event.values[0]);
        }

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            sensorReading.setGameRotationX(event.values[0]);
            sensorReading.setGameRotationY(event.values[1]);
            sensorReading.setGameRotationZ(event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            sensorReading.setGeomagneticRotationX(event.values[0]);
            sensorReading.setGeomagneticRotationY(event.values[1]);
            sensorReading.setGeomagneticRotationZ(event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorReading.setMagneticFieldX(event.values[0]);
            sensorReading.setMagneticFieldY(event.values[1]);
            sensorReading.setMagneticFieldZ(event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            sensorReading.setProximity(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        unregisterReceiver(broadcastReceiver);
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
        timer.schedule(doAsynchronousTask, 3000, 5000); //execute in every 5000 ms
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
        sensorReading.setImInTram(simpleSwitch.isChecked());
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sensorReading.setImInTram(isChecked);
            }
        });
    }

    private class PhoneLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            sensorReading.setLatitude(loc.getLatitude());
            sensorReading.setLongitude(loc.getLongitude());
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
        @Override
        protected Void doInBackground(Void... voids) {
            HttpPostPackage();
            return null;
        }
    }

    private void HttpPostPackage() {
        if (this.readingsJsonArray.size() < 10) {
            sensorReading.setNearestBeaconId(beaconData.getBeaconId());
            this.readingsJsonArray.add(SerializationUtils.clone(sensorReading));

        } else {
            String json = new Gson().toJson(readingsJsonArray);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            SensorsReadingService service = RetrofitClientInstance.getRetrofitInstance().create(SensorsReadingService.class);
            String token = "Bearer " + preferences.getString(TOKEN_FIELD, "");
            Call<Void> call = service.postMultipleReadings(body, token);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 201) {
                        postCounterTextView.setText(String.valueOf(++postCounter));
                    } else {
                        Toast.makeText(SensorsActivity.this, "Failed while sending readings", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });

            this.readingsJsonArray.clear();
        }
    }

    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        private final static String BATTERY_LEVEL = "level";

        @Override
        public void onReceive(Context context, Intent intent) {
            sensorReading.setBatteryLevel(intent.getIntExtra(BATTERY_LEVEL, 0));
        }
    }
}