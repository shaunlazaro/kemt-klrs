package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeScreen extends AppCompatActivity {

    String TAG_API = "ApiError";
    public static final String ROUTINE_TAG = "ROUTINE";

    Pose pose;

    ImageButton exerciseButton;
    TextView exerciseText;
    TextView usernameText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        exerciseButton = findViewById(R.id.exercise_button);
        exerciseText = findViewById(R.id.exercise_text);
        usernameText = findViewById(R.id.username);

        // for now just display the username that was entered in the login screen
        String loginUsername = getIntent().getStringExtra(MainMenu.USERNAME_TAG);
        if (loginUsername != null) {
            usernameText.setText(loginUsername);
        }

        // implement button to get to today's exercises
        exerciseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent exerciseIntent = new Intent(HomeScreen.this, MyExercises.class);
                if (pose != null) {
                    exerciseIntent.putExtra(ROUTINE_TAG, pose);
                }

                HomeScreen.this.startActivity(exerciseIntent);
            }
        });

        // load data from API
        loadData();
    }

    private void loadData() {
        // create retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://140.238.151.117:8000/api/routine-configs/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // get routine
        Call<Pose> call = apiService.getRoutine(1);

        if (call != null) {
            call.enqueue(new Callback<Pose>() {
                @Override
                public void onResponse(Call<Pose> call, Response<Pose> response) {
                    if (!response.isSuccessful()) {
                        // Handle the error scenario here
                        Log.e(TAG_API, "Response Code: " + response.code());
                        return;
                    }

                    pose = response.body();

                    // TODO When pulling a routine, set username to api username

                    // TODO set the exercise text to match pulled routine

                    Log.d(TAG_API, pose.getId().toString());
                    Log.d(TAG_API, pose.getLandmarks().get(0).toString());
                }

                @Override
                public void onFailure(Call<Pose> call, Throwable t) {
                    Log.e(TAG_API, t.toString());
                }
            });
        }
    }
}