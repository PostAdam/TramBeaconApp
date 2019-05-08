package com.pp.beacon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.pp.retrofit.LoginService;
import com.pp.retrofit.RetrofitClientInstance;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.prefs.Preferences;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private EditText passwordEdt;
    private EditText emailEdt;

    private static final String JWT_PREFERENCES = "jwtPreferences";
    private static final String TOKEN_FIELD = "token";
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences(JWT_PREFERENCES, MODE_PRIVATE);
        if(preferences.contains(TOKEN_FIELD)) {
            //check if token didn't expire
            startActivity(new Intent(this, MainActivity.class));
        }

        passwordEdt = findViewById(R.id.passwordText);
        emailEdt = findViewById(R.id.emailText);


    }

    public void login(View view) throws JSONException {
        final String email = emailEdt.getText().toString();
        final String password = passwordEdt.getText().toString();
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("email", email);
        loginJSON.put("password", password);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), loginJSON.toString());
        LoginService service = RetrofitClientInstance.getRetrofitInstance().create(LoginService.class);
        Call<String> call = service.login(body);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200) {
                    String token = response.body();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(TOKEN_FIELD, token);
                    editor.commit();
                    try {
                        DecodeJWT(token);
                    } catch(JSONException ex) {
                        Log.e("JSONException", ex.getMessage());
                    }
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error during login", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void DecodeJWT(String jwt) throws JSONException{
        String[] splited = jwt.split(("\\."));
        String body = splited[1];

        String decodedBody = new String(Base64.decode(body, Base64.URL_SAFE));

        JSONObject jsonBody = new JSONObject(decodedBody);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putString("UserId", jsonBody.getString("nameid"));
        editor.commit();
    }
}