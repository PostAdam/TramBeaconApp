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
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.RegionUtils;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.pp.model.BeaconToken;
import com.pp.model.SensorReadingBase;
import com.pp.model.SensorReadingsBundle;
import com.pp.retrofit.BeaconTokenService;
import com.pp.retrofit.RetrofitClientInstance;
import com.pp.retrofit.SensorsReadingService;
import com.pp.retrofit.TramTravelService;
import com.pp.services.BluetoothService;
import com.pp.services.FileService;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TramTravelDetectionActivity extends AppCompatActivity implements SensorEventListener {
    // region Privates

    private BeaconManager beaconManager;
    private BeaconRegion region;
    private final UUID BEACON_UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    private Beacon nearestBeacon;
    private String token;

//  raspberry
    private final String raspberryMacAddress = "B8:27:EB:24:74:91";

//  raspberry
    private Integer minor = 65535;
    private Integer major = 65535;

//    beacon
//    private final String raspberryMacAddress = "CC:94:9A:0F:8F:B1";

//    beacon
//    private Integer minor = 55826;
//    private Integer major = 11364;

    private double raspberryLatitude;
    private double raspberryLongitude;
    private int outOfTramRangeCounter = 0;
    private TramTravelDetectionActivity.HTTPAsyncTask asyncTask;

    List<SensorReadingBase> sensorsReadings = new ArrayList<>();
    private SensorReadingBase sensorReading;

    private TramTravelService tramTravelService;
    private SensorsReadingService sensorsReadingService;
    private SensorManager sensorManager;

    private TextView postCounterTextView;
    private TextView amIInTramTextView;
    private TextView beaconIdTextView;
    private TextView beaconProximityTextView;
    private Switch useNeuralNetworkSwitch;
    private Switch useLocationSwitch;

    private int postCounter = 0;
    private boolean amIInTram = false;
    private boolean isPostReadingsTaskStarted = false;
    private boolean isTravelStarted = false;

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
        beaconIdTextView = findViewById(R.id.nearestBeaconId);
        beaconProximityTextView = findViewById(R.id.nearestBeaconProximity);

        postCounterTextView.setText(String.valueOf(postCounter));
        amIInTramTextView.setText(String.valueOf(amIInTram));

        tramTravelService = RetrofitClientInstance.getRetrofitInstance().create(TramTravelService.class);
        sensorsReadingService = RetrofitClientInstance.getRetrofitInstance().create(SensorsReadingService.class);

        token = "Bearer " + preferences.getString(TOKEN_FIELD, "");
        callGetAllBeaconTokens();

        setUpBeaconRanging();
        setUpSwitches();
        setUpBackButton();
        setUpSensors();
        setUpLocalization();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
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
        asyncTask.cancel(true);
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
                            if (asyncTask != null && asyncTask.isCancelled()) {
                                return;
                            }
                            asyncTask = new TramTravelDetectionActivity.HTTPAsyncTask();
                            asyncTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000); //execute in every 5000 ms
    }

    private void setUpBeaconRanging() {
        beaconManager = new BeaconManager(this);
        region = new BeaconRegion("ranged region", major == null && minor == null ? BEACON_UUID : null, major, minor);
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                for (Beacon beacon : list) {
                    if (beacon.getMacAddress().toStandardString().equals(raspberryMacAddress)) {
                        nearestBeacon = beacon;
                        beaconIdTextView.setText(String.valueOf(beacon.getUniqueKey()));
                        beaconProximityTextView.setText(String.valueOf(RegionUtils.computeAccuracy(beacon)));
                        setVehicleLocation(beacon.getProximityUUID().toString());

                        if (isNetworkConnectionEnabled() && !isPostReadingsTaskStarted) {
                            isPostReadingsTaskStarted = true;
                            callAsynchronousTask();
                        }
                    }
                }
            }
        });
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

    private void setUpSwitches() {
        useLocationSwitch = findViewById(R.id.useLocationSwitch);
        useNeuralNetworkSwitch = findViewById(R.id.useNeuralNetworkSwitch);
    }

    private void setVehicleLocation(String key) {
        key = "0005014b-5514-0001-093d-122800000000";
        key = key.replace("-", "");
        String longitudeFromKey = key.substring(0, 12);
        String latitudeFromKey = key.substring(12, 24);

        raspberryLongitude = transformStringToPosition(longitudeFromKey.toLowerCase());
        raspberryLatitude = transformStringToPosition(latitudeFromKey.toLowerCase());
    }

    private double transformStringToPosition(String position) {
        String[] positionChunks = splitIntoTwoElementChunks(position);

        StringBuilder stringBuilder = new StringBuilder();
        if (positionChunks[0].equals("ff")) {
            stringBuilder.append("-");
        }
        stringBuilder.append(Integer.parseInt(positionChunks[1], 16));
        if (!positionChunks[2].equals("00")) {
            stringBuilder.append(Integer.parseInt(positionChunks[2], 16));
        }
        stringBuilder.append(".");

        for (int i = 3; i < positionChunks.length; i++) {
            if (positionChunks[i].startsWith("0")) {
                stringBuilder.append("0");
                stringBuilder.append(positionChunks[i].toCharArray()[1]);
            } else {
                stringBuilder.append(Integer.parseInt(positionChunks[i], 16));
            }
        }

        return Double.valueOf(stringBuilder.toString());
    }

    private String[] splitIntoTwoElementChunks(String position) {
        String[] positionChunks = new String[position.length() / 2];
        for (int i = 0; i < positionChunks.length; i++) {
            positionChunks[i] = position.substring(i * 2, i * 2 + 2);
        }
        return positionChunks;
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
        sensorReading.setNearestBeaconId(nearestBeacon != null ? nearestBeacon.getProximityUUID().toString() : "No beacon here");
        sensorReading.setVehicleLongitude(raspberryLongitude);
        sensorReading.setVehicleLatitude(raspberryLatitude);
        sensorReading.setTimeStamp(new Timestamp(new Date().getTime()));
        sensorsReadings.add(sensorReading);
        if (sensorsReadings.size() <= 5) {
            return;
        }

        callAmIInTram(sensorsReadings);
        sensorsReadings.clear();
    }

    private void callGetAllBeaconTokens() {
        BeaconTokenService beaconTokenService = RetrofitClientInstance.getRetrofitInstance().create(BeaconTokenService.class);
        Call<List<BeaconToken>> call = beaconTokenService.getAllBeaconTokens(token);
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
    }

    private void callAmIInTram(List<SensorReadingBase> sensorsReadings) {
        SensorReadingsBundle bundle = new SensorReadingsBundle(sensorsReadings, useNeuralNetworkSwitch.isChecked(), useLocationSwitch.isChecked());
        String json = new Gson().toJson(bundle);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Call<Object> call = sensorsReadingService.amIInTram(body, token);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.code() == 200) {
                    amIInTram = ((LinkedTreeMap<String, Boolean>) (response.body())).get("amIInTram");
                    postCounterTextView.setText(String.valueOf(++postCounter));
                    amIInTramTextView.setText(String.valueOf(amIInTram));

                    if (amIInTram && nearestBeacon != null) {
                        outOfTramRangeCounter = 0;
                        if (!isTravelStarted) {
                            isTravelStarted = true;
                            callStartTravel();
                        }
                    } else {
                        ++outOfTramRangeCounter;
                        if (outOfTramRangeCounter >= 5) {
                            callFinishTravel();
                        }
                    }
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

    private void callStartTravel() {
        String json = new Gson().toJson(nearestBeacon.getProximityUUID().toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Call<Void> startTravel = tramTravelService.startTravel(body, token);
        startTravel.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(TramTravelDetectionActivity.this, "Started travel successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TramTravelDetectionActivity.this, "Failed while starting travel", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void callFinishTravel() {
        RequestBody emptyBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");
        Call<Object> finishTravel = tramTravelService.finishTravel(emptyBody, token);
        finishTravel.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Toast.makeText(TramTravelDetectionActivity.this, "Travel ended successfully", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(TramTravelDetectionActivity.this, "Failed while finishing travel", Toast.LENGTH_LONG).show();
            }
        });
    }
}