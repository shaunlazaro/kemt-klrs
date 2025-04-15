package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeScreen extends AppCompatActivity {

    public static final String TAG_API = "ApiError";
    public static final String ROUTINE_TAG = "ROUTINE";

    public static RoutineConfig routineConfig = new RoutineConfig();
    public static List<RoutineData> routineData = new ArrayList<>();
    public static boolean routineDataLoaded = false;

    BottomNavigationView bottomNavigationView;

    ImageButton exerciseButton;
    Button exerciseTextButton;
    TextView usernameText;

    String ROUTNIE_ID = "3";

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

        routineDataLoaded = false;

        exerciseButton = findViewById(R.id.exercise_button);
        exerciseTextButton = findViewById(R.id.exercise_text_button);
        usernameText = findViewById(R.id.username);

        // bottom navigation functionality
        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.nav_progress) {
                replaceFragment(new ProgressFragment());
            } else if (item.getItemId() == R.id.nav_account) {
                replaceFragment(new AccountFragment());
            }

            return true;
        });

        // update welcome back user
        String firstName = MainMenu.name != null ? MainMenu.name.split(" ")[0] : "";
        usernameText.setText(firstName);

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
        // http://140.238.151.117:8000/api/routine-data/ for routine data
        // iterate "created_at" field for last week (maybe library that will get day of week)
        // populate progress screen

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // create retrofit instance for both routine configs and routine data
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://140.238.151.117:8000/api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // get routine
        Call<List<RoutineConfig>> call = apiService.getRoutine(MainMenu.tokenId);
        Call<List<RoutineData>> callData = apiService.getRoutineData(MainMenu.tokenId);

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

                    assert response.body() != null;
                    routineConfig = response.body().stream()
                            .filter(x -> Objects.equals(x.getId(), ROUTNIE_ID))
                            .findFirst().orElse(null); // hardcode id
                    Log.d("TESTNICK", routineConfig.getId());


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

        if (callData != null) {
            callData.enqueue(new Callback<List<RoutineData>>() {
                @Override
                public void onResponse(Call<List<RoutineData>> call, Response<List<RoutineData>> response) {
                    if (!response.isSuccessful()) {
                        // Handle the error scenario here
                        Log.e(TAG_API, "Response Code: " + response.code());
                        Log.d(TAG_API, call.request().url().toString());
                        return;
                    }

                    assert response.body() != null;

                    routineData = response.body();
                    routineDataLoaded = true;

                    if (bottomNavigationView.getSelectedItemId() == R.id.nav_progress) {
                        ProgressFragment progressFragment = (ProgressFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_frame_layout);
                        progressFragment.doneLoading();
                    }
                }

                @Override
                public void onFailure(Call<List<RoutineData>> call, Throwable t) {
                    Log.e(TAG_API, t.toString());
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame_layout, fragment);
        fragmentTransaction.commit();
    }
}