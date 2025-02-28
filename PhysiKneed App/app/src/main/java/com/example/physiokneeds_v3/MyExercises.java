package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyExercises extends AppCompatActivity {

    public static final String IS_CASTED = "CASTORNOT";

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
        Button startCastExercises = findViewById(R.id.start_cast_button);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton backButton = findViewById(R.id.back_button);

        // get routine from previous screen
        Pose pose = (Pose) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);

        // TODO Set the text and images to the correct routine

        startExercises.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyExercises.this, SetUpDevice.class);

                intent.putExtra(IS_CASTED, true);

                MyExercises.this.startActivity(intent);
            }
        });

        startCastExercises.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyExercises.this, SetUpDevice.class);

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
}