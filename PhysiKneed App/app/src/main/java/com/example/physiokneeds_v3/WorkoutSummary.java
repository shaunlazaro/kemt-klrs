package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkoutSummary extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView timeSpent = findViewById(R.id.time_spent);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        LinearLayout linearLayoutSummary = findViewById(R.id.completeLayout);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        LinearLayout linearLayoutScore = findViewById(R.id.linear_layout_score);

        ProgressBar scoreBar = findViewById(R.id.progress_circular);
        TextView scoreText = findViewById(R.id.score_text);

        Button finishButton = findViewById(R.id.finish_button);

        // get routine from previous screen
        RoutineConfig routineConfig = (RoutineConfig) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);
        RoutineDataUpload routineData = (RoutineDataUpload) getIntent().getSerializableExtra("RoutineData");

        List<Double> exerciseScores = new ArrayList<>();
        double repScore;
        double exerciseScore;

        double totalTime = 0;

        for(int i = 0; i < routineData.getRoutineComponentData().size(); i++) {
            exerciseScore = 0;
            for (int j = 0; j < routineData.getRoutineComponentData().get(i).getRepData().size(); j++) {
                RepData repData = routineData.getRoutineComponentData().get(i).getRepData().get(j);
                boolean romPenalty = !repData.isGoalExtensionMet() || !repData.isGoalFlexionMet();
                repScore = repData.getScore();
                Log.d("SCORE_DEBUG", "Rep Max Score: " + String.valueOf(repScore));
                Log.d("SCORE_DEBUG", "Alert Size: " + String.valueOf(repData.getAlerts().size()));

                if (romPenalty) {
                    Log.d("SCORE_DEBUG", "ROM penalty called");
                    repScore += 0.2;
                }
                repScore -= repData.getAlerts().size()*0.2;
                Log.d("SCORE_DEBUG", "repScore: " + String.valueOf(repScore));

                exerciseScore += repScore;
                Log.d("SCORE_DEBUG", "exerciseScore: " + String.valueOf(exerciseScore));
                // total time
                totalTime += repData.getTotalTime();
            }
            // Convert from total rep scores to average rep score.
            exerciseScore /= routineData.getRoutineComponentData().get(i).getRepData().size();
            Log.d("SCORE_DEBUG", "Average rep scores: " + String.valueOf(exerciseScore));

            exerciseScores.add(exerciseScore);
        }

        // calculate overall score
        double overallSum = 0;
        for (double score : exerciseScores) {
            overallSum += score;
        }
        double overallScore = overallSum / exerciseScores.size();

        scoreText.setText(String.valueOf((int) Math.round(overallScore*100)));
        scoreBar.setProgress((int) Math.round(overallScore*100));

        // timer
        String timeDisplay = "";
        if (totalTime < 60) {
            timeDisplay = ((int) totalTime) + "s";
        } else {
            timeDisplay = Math.floor(totalTime / 60) + "m " + ((int)totalTime%60) + "s";
        }

        timeSpent.setText(timeDisplay);

        for (int i = 0; i < Objects.requireNonNull(routineConfig).getExercises().size(); i++) {
            // Create a new Frame layout for score
            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            frameLayout.setPadding(0, dpToPx(10), 0, 0);

            // Create exercise textviews
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            textParams.gravity = Gravity.START;
            textView.setLayoutParams(textParams);
            String text =
                    routineData.getRoutineComponentData().get(i).getRepData().size() +
                    "/" + routineConfig.getExercises().get(i).getReps() + " " +
                    routineConfig.getExercises().get(i).getExercise().getDisplayName();
            textView.setText(text);
            textView.setTextSize(14);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.source_sans));
            textView.setTextColor(ContextCompat.getColor(this, R.color.black));
            textView.setPadding(dpToPx(50), 0, dpToPx(40), 0);
            textView.setCompoundDrawablePadding(dpToPx(10));
            textView.setGravity(Gravity.CENTER);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, ContextCompat.getDrawable(this, R.drawable.circle_check), null, null, null);

            TextView textView2 = new TextView(this);
            textView2.setLayoutParams(textParams);
            textView2.setText(routineConfig.getExercises().get(i).getExercise().getDisplayName());
            textView2.setTextSize(14);
            textView2.setTypeface(ResourcesCompat.getFont(this, R.font.source_sans));
            textView2.setTextColor(ContextCompat.getColor(this, R.color.black));
            textView2.setGravity(Gravity.CENTER);

            TextView textView3 = new TextView(this);
            FrameLayout.LayoutParams textParamsEnd = new FrameLayout.LayoutParams(
                    dpToPx(22),
                    dpToPx(22));
            textParamsEnd.gravity = Gravity.END;
            textView3.setLayoutParams(textParamsEnd);
            textView3.setText(String.valueOf((int) Math.round(exerciseScores.get(i)*100)));
            textView3.setTextSize(12);
            textView3.setTypeface(ResourcesCompat.getFont(this, R.font.source_sans), Typeface.BOLD);
            textView3.setTextColor(ContextCompat.getColor(this, R.color.white));
            textView3.setBackground(ContextCompat.getDrawable(this, R.drawable.score_circle));
            textView3.setGravity(Gravity.CENTER);

            frameLayout.addView(textView2);
            frameLayout.addView(textView3);

            linearLayoutSummary.addView(textView, 3+i);
            linearLayoutScore.addView(frameLayout, 4+i);
        }

        finishButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WorkoutSummary.this, HomeScreen.class);
                WorkoutSummary.this.startActivity(intent);
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}