package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeScreen extends AppCompatActivity {

    String TAG_API = "ApiError";
    public static final String ROUTINE_TAG = "ROUTINE";

    RoutineConfig routineConfig;

    ImageButton exerciseButton;
    Button exerciseTextButton;
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
        exerciseTextButton = findViewById(R.id.exercise_text_button);
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
                if (routineConfig != null) {
                    exerciseIntent.putExtra(ROUTINE_TAG, routineConfig);
                }

                HomeScreen.this.startActivity(exerciseIntent);
            }
        });

        exerciseTextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent exerciseIntent = new Intent(HomeScreen.this, MyExercises.class);
                if (routineConfig != null) {
                    exerciseIntent.putExtra(ROUTINE_TAG, routineConfig);
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
        Call<List<RoutineConfig>> call = apiService.getRoutine();

        if (call != null) {
            call.enqueue(new Callback<List<RoutineConfig>>() {
                @Override
                public void onResponse(Call<List<RoutineConfig>> call, Response<List<RoutineConfig>> response) {
                    if (!response.isSuccessful()) {
                        // Handle the error scenario here
                        Log.e(TAG_API, "Response Code: " + response.code());
                        Log.d(TAG_API, call.request().url().toString());
                        return;
                    }

                    routineConfig = response.body().get(0);

                    // TODO When pulling a routine, set username to api username

                    String exerciseList = "\n";
                    for (int i = 0; i < routineConfig.getExercises().size(); i++) {
                        // formatting example
                        // android:text="\n\nSeated Leg Extension x 8\nSquat x 6\nStanding Quad Stretch x 10\nCalf Raise x 10"
                        // \n\nSeated Leg Extension x 8\nSquat x 6\nStanding Quad Stretch x 10\nCalf Raise x 10
                        exerciseList += "\n"
                                + routineConfig.getExercises().get(i).getExercise().getDisplayName()
                                + " x "
                                + routineConfig.getExercises().get(i).getReps();
                    }

                    exerciseTextButton.setText(exerciseList);

                    // test, output name
                    Log.d(TAG_API, routineConfig.getName());
                }

                @Override
                public void onFailure(Call<List<RoutineConfig>> call, Throwable t) {
                    Log.e(TAG_API, t.toString());
                }
            });
        }
    }
}