package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WorkoutInstructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_instructions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView exerciseTitle = findViewById(R.id.exercise_instr_id);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView repsCount = findViewById(R.id.reps_text_instr);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView image = findViewById(R.id.exercise_img);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView steps = findViewById(R.id.instr_text);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView commonErrors = findViewById(R.id.comm_errs);

        RoutineConfig routineConfig = (RoutineConfig) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);
        int exerciseNameId = (int) getIntent().getSerializableExtra("EXERCISE_NAME");

        assert routineConfig != null;

        exerciseTitle.setText(routineConfig.getExercises().get(exerciseNameId).getExercise().getDisplayName());
        repsCount.setText(routineConfig.getExercises().get(exerciseNameId).getReps().toString() + " reps");

        if (routineConfig.getExercises().get(exerciseNameId).getExercise().getDisplayName().equals("Squat")) {
            image.setImageResource(R.drawable.squat);
            steps.setText("1. Stand with feet shoulder-width apart\n\n2. Lower your body by bending at the knees\n    and hips, as if you are sitting in a chair\n\n3. Aim to get your thighs parallel to the\n    ground\n\n4. Return to a standing position");
            commonErrors.setText("Maintain a neutral spine throughout the movement and hold your core tight\n\nKnees should point slightly outwards, aligned with toes");
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });



    }
}