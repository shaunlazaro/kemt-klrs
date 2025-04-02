package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class WorkoutComplete extends AppCompatActivity {

    int exerciseIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_complete);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get routine data
        RoutineDataUpload routineData = (RoutineDataUpload) getIntent().getSerializableExtra("RoutineData");
        if (routineData == null) {
            // if workout was not completed, go back to home screen
            Intent intent = new Intent(WorkoutComplete.this, HomeScreen.class);
            WorkoutComplete.this.startActivity(intent);
        } else {
            Log.d("SEND_DATA", String.valueOf(routineData.getRoutineComponentData().get(0).getRepData().get(0).getScore()));
        }

        TextView questionText = findViewById(R.id.question_text);
        ImageView exerciseImage = findViewById(R.id.exercise_image);
        TextView exerciseText = findViewById(R.id.exercise_text);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        SeekBar scoreBar = findViewById(R.id.seekBar);
        Button nextButton = findViewById(R.id.next_button);
        TextView skipText = findViewById(R.id.skip_button);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView backText = findViewById(R.id.back_text);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText editText = findViewById(R.id.thoughts_box);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        FrameLayout faces = findViewById(R.id.faces_images);

        RoutineConfig routineConfig = (RoutineConfig) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);
        List<RoutineComponent> exerciseList = routineConfig.getExercises();

        exerciseText.setText(exerciseList.get(exerciseIndex).getExercise().getDisplayName());
        switch (exerciseList.get(exerciseIndex).getExercise().getDisplayName()) {
            case "Seated Leg Extension (Right)":
                exerciseImage.setImageResource(R.drawable.seated_leg);
                break;
            case "Squat":
                exerciseImage.setImageResource(R.drawable.squat);
                break;
            case "Hamstring Curl (Right)":
                exerciseImage.setImageResource(R.drawable.hamstring_curl);
                break;
            default:
                exerciseImage.setImageResource(R.drawable.baseline_fitness_center_24);
                break;
        }

        scoreBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar scoreBar, int progress, boolean fromUser) {
                nextButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO store the data somewhere,
                //  make it display the stored data since the back button can be pressed
                exerciseIndex++;
                if (exerciseIndex == exerciseList.size()) {
                    questionText.setText("Any thoughts to record?");
                    exerciseImage.setVisibility(View.GONE);
                    exerciseText.setVisibility(View.GONE);
                    scoreBar.setVisibility(View.GONE);
                    backText.setVisibility(View.VISIBLE);
                    faces.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                } else if (exerciseIndex < exerciseList.size()){
                    switch (exerciseList.get(exerciseIndex).getExercise().getDisplayName()) {
                        case "Seated Leg Extension (Right)":
                            exerciseImage.setImageResource(R.drawable.seated_leg);
                            break;
                        case "Squat":
                            exerciseImage.setImageResource(R.drawable.squat);
                            break;
                        case "Hamstring Curl (Right)":
                            exerciseImage.setImageResource(R.drawable.hamstring_curl);
                            break;
                        default:
                            exerciseImage.setImageResource(R.drawable.baseline_fitness_center_24);
                            break;
                    }
                    exerciseText.setText(exerciseList.get(exerciseIndex).getExercise().getDisplayName());
                    backText.setVisibility(View.VISIBLE);
                    scoreBar.setProgress(1);
                    nextButton.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(WorkoutComplete.this, WorkoutSummary.class);
                    intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig);
                    intent.putExtra("RoutineData", routineData);

                    WorkoutComplete.this.startActivity(intent);
                }

            }
        });

        skipText.setOnClickListener(v -> {
            // TODO send user to workout summary
            Intent intent = new Intent(WorkoutComplete.this, WorkoutSummary.class);
            intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig);
            intent.putExtra("RoutineData", routineData);

            WorkoutComplete.this.startActivity(intent);
        });

        backText.setOnClickListener(v -> {
            exerciseIndex--;
            if (exerciseIndex == 0) {
                backText.setVisibility(View.GONE);
            }
            scoreBar.setProgress(1);
            questionText.setText("How easy did that feel?");
            exerciseImage.setVisibility(View.VISIBLE);
            switch (exerciseList.get(exerciseIndex).getExercise().getDisplayName()) {
                case "Seated Leg Extension (Right)":
                    exerciseImage.setImageResource(R.drawable.seated_leg);
                    break;
                case "Squat":
                    exerciseImage.setImageResource(R.drawable.squat);
                    break;
                case "Hamstring Curl (Right)":
                    exerciseImage.setImageResource(R.drawable.hamstring_curl);
                    break;
                default:
                    exerciseImage.setImageResource(R.drawable.baseline_fitness_center_24);
                    break;
            }
            exerciseText.setVisibility(View.VISIBLE);
            scoreBar.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            faces.setVisibility(View.VISIBLE);
            exerciseText.setText(exerciseList.get(exerciseIndex).getExercise().getDisplayName());
            nextButton.setVisibility(View.GONE);
        });
    }
}