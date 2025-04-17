package com.example.physiokneeds_v3;

import static com.example.physiokneeds_v3.HomeScreen.ROUTINE_TAG;
import static com.example.physiokneeds_v3.HomeScreen.TAG_API;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeFragment extends Fragment {
    RoutineConfig routineConfig = HomeScreen.routineConfig;
    ImageButton exerciseButton;
    Button exerciseTextButton;
    TextView usernameText;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        exerciseButton = view.findViewById(R.id.exercise_button);
        exerciseTextButton = view.findViewById(R.id.exercise_text_button);
        usernameText = view.findViewById(R.id.username);

        // implement button to get to today's exercises
        exerciseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent exerciseIntent = new Intent(getActivity(), MyExercises.class);
                if (routineConfig != null) {
                    exerciseIntent.putExtra(ROUTINE_TAG, routineConfig);
                }

                startActivity(exerciseIntent);
            }
        });

        // update welcome back user
        String firstName = MainMenu.name != null ? MainMenu.name.split(" ")[0] : "";
        usernameText.setText(firstName);

        exerciseTextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent exerciseIntent = new Intent(getActivity(), MyExercises.class);
                if (routineConfig != null) {
                    exerciseIntent.putExtra(ROUTINE_TAG, routineConfig);
                }

                startActivity(exerciseIntent);
            }
        });

        loadData();

        // Inflate the layout for this fragment
        return view;
    }

    private void loadData() {
        String exerciseList = "\n";

        if (routineConfig.getExercises() == null) {
            return;
        }

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
    }
}