package com.pp.beacon;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.pp.model.BeaconData;
import com.pp.model.BeaconToken;
import com.pp.model.SensorReadingBase;
import com.pp.retrofit.BeaconTokenService;
import com.pp.retrofit.RetrofitClientInstance;
import com.pp.retrofit.SensorsReadingService;
import com.pp.services.BluetoothService;
import com.pp.services.FileService;

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

public class TramTravelDetectionActivity extends AppCompatActivity implements SensorEventListener {
    // region Privates

    private SensorManager sensorManager;

    private SensorReadingBase sensorReading;
    private BeaconData beaconData;
    private TextView postCounterTextView;
    private TextView amIInTramTextView;
    private final String raspberryMacAddress = "00:34:DA:42:FC:5F";
    //    private final String raspberryMacAddress = "CC:94:9A:0F:8F:B1";
    private int postCounter = 0;
    private boolean amIInTram = false;

    private static final String JWT_PREFERENCES = "jwtPreferences";
    private static final String TOKEN_FIELD = "token";
    private SharedPreferences preferences;

    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tram_travel_detection);

        sensorReading = new SensorReadingBase();
        preferences = getSharedPreferences(JWT_PREFERENCES, MODE_PRIVATE);
        postCounterTextView = findViewById(R.id.postCounter);
        amIInTramTextView = findViewById(R.id.amIInTram);
        postCounterTextView.setText(String.valueOf(postCounter));
        amIInTramTextView.setText(String.valueOf(amIInTram));

        BeaconTokenService service = RetrofitClientInstance.getRetrofitInstance().create(BeaconTokenService.class);
        String token = "Bearer " + preferences.getString(TOKEN_FIELD, "");
        Call<List<BeaconToken>> call = service.getAllBeaconTokens(token);
        call.enqueue(new Callback<List<BeaconToken>>() {
            @Override
            public void onResponse(Call<List<BeaconToken>> call, Response<List<BeaconToken>> response) {
                List<BeaconToken> beaconTokens = response.body();
                URI fileUri = FileService.createAndWriteFile(getApplicationContext().getCacheDir(), "beaconTokens", BeaconToken.tokensToString(beaconTokens));
                BluetoothService bluetoothService = new BluetoothService(raspberryMacAddress, fileUri);
            }

            @Override
            public void onFailure(Call<List<BeaconToken>> call, Throwable t) {
                Toast.makeText(TramTravelDetectionActivity.this, "Failed to fetch tokens!", Toast.LENGTH_LONG).show();
            }
        });

        setUpBackButton();
        setUpSensors();
        setUpLocalization();

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
            sensorReading.setAccelerometerValues(event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorReading.setGyroscopeValues(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            TramTravelDetectionActivity.HTTPAsyncTask task = new TramTravelDetectionActivity.HTTPAsyncTask();
                            task.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        // TODO: set delay and period
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
    }

    private void setUpLocalization() {
        LocationManager locationManager;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, new TramTravelDetectionActivity.PhoneLocationListener());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        sensorReading.setNearestBeaconId(beaconData.getBeaconId());
        sensorReading.setTimeStamp(new Timestamp(new Date().getTime()));
        ArrayList<SensorReadingBase> readings = new ArrayList<>();
        readings.add(sensorReading);

        String json = new Gson().toJson(readings);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        SensorsReadingService service = RetrofitClientInstance.getRetrofitInstance().create(SensorsReadingService.class);
        String token = "Bearer " + preferences.getString(TOKEN_FIELD, "");
        Call<Object> call = service.amIInTram(body, token);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.code() == 200) {
                    amIInTram = ((LinkedTreeMap<String, Boolean>) (response.body())).get("amIInTram");
                    postCounterTextView.setText(String.valueOf(++postCounter));
                    amIInTramTextView.setText(String.valueOf(amIInTram));
                } else {
                    Toast.makeText(TramTravelDetectionActivity.this, "Failed while sending readings with error code " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(TramTravelDetectionActivity.this, "Failed while sending readings", Toast.LENGTH_LONG).show();
            }
        });
    }
}