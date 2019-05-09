package com.pp.beacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pp.model.User;
import com.pp.retrofit.AuthService;
import com.pp.retrofit.RetrofitClientInstance;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.nameEDT)
    public EditText nameEDT;
    @BindView(R.id.lastnameEDT)
    public EditText lastnameEDT;
    @BindView(R.id.emailEDT)
    public EditText emaliEDT;
    @BindView(R.id.passwordEDT)
    public EditText passwordEDT;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);
            ButterKnife.bind(this);
        }

        public void register(View view) throws JSONException {
            User user = new User(nameEDT.getText().toString(), lastnameEDT.getText().toString(), emaliEDT.getText().toString(), passwordEDT.getText().toString());

            String json = new Gson().toJson(user);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            AuthService service = RetrofitClientInstance.getRetrofitInstance().create(AuthService.class);
            Call<Void> call = service.register(body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Register failed!", Toast.LENGTH_LONG).show();
                }
            });
        }
}
