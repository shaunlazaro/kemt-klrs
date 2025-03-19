package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyExercises extends AppCompatActivity {

    public static final String IS_CASTED = "CASTORNOT";

    LinearLayout layoutEx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_exercises);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button startExercises = findViewById(R.id.start_button);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton backButton = findViewById(R.id.back_button);

        // get routine from previous screen
        RoutineConfig routineConfig = (RoutineConfig) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);

        // Set the text and images to the correct routine
        layoutEx = findViewById(R.id.linear_layout_exercises);

        for (int i = 0; i < routineConfig.getExercises().size(); i++) {
            // Create a new LinearLayout
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setPadding(0, dpToPx(25), 0, 0);

            // Create a text LinearLayout
            LinearLayout linearLayoutText = new LinearLayout(this);
            linearLayoutText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayoutText.setOrientation(LinearLayout.VERTICAL);
//            linearLayoutText.setPadding(0, dpToPx(25), 0, 0);

            // Create ImageView
            // TODO make imageViews buttons for more exercise instructions and custom images
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    dpToPx(58),
                    dpToPx(58));
            imageParams.gravity = Gravity.CENTER;
            imageParams.setMargins(dpToPx(40), 0, 0, 0); // layout_marginStart="40dp"
            imageView.setLayoutParams(imageParams);
            imageView.setBackgroundResource(R.drawable.outline_button); // Set background drawable
            if (routineConfig.getExercises().get(i).getExercise().getDisplayName().equals("Seated Leg Extension (Left)")) {
                imageView.setImageResource(R.drawable.seated_leg);
            } else if (routineConfig.getExercises().get(i).getExercise().getDisplayName().equals("Squat")) {
                imageView.setImageResource(R.drawable.squat);
            } else {
                imageView.setImageResource(R.drawable.baseline_fitness_center_24);
            }
            imageView.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            // Create TextView
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            textParams.setMargins(dpToPx(10), 0, 0, 0); // layout_marginStart="10dp"
            textView.setLayoutParams(textParams);
            textView.setText(routineConfig.getExercises().get(i).getExercise().getDisplayName());
            textView.setTextSize(20);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.source_sans), Typeface.BOLD);
            textView.setTextColor(ContextCompat.getColor(this, R.color.black));
            textView.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(5));

            // create rep textview
            TextView repsText = new TextView(this);
            repsText.setLayoutParams(textParams);
            repsText.setText(routineConfig.getExercises().get(i).getReps() + " reps");
            repsText.setTextSize(14);
            repsText.setTypeface(ResourcesCompat.getFont(this, R.font.source_sans));
            repsText.setTextColor(ContextCompat.getColor(this, R.color.black));
            repsText.setPadding(dpToPx(10), dpToPx(0), dpToPx(0), dpToPx(0));

            // Add views to the LinearLayout
            linearLayoutText.addView(textView);
            linearLayoutText.addView(repsText);
            linearLayout.addView(imageView);
            linearLayout.addView(linearLayoutText);

            // Add the LinearLayout to the parent layout
            layoutEx.addView(linearLayout, 1+i);
        }

        startExercises.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyExercises.this, SetUpDevice.class);
                intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig);
                intent.putExtra(IS_CASTED, true);

                MyExercises.this.startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyExercises.this, HomeScreen.class);

                MyExercises.this.startActivity(intent);
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}