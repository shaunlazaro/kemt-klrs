package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainMenu extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMenu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            Python.start(new AndroidPlatform(getApplicationContext()));

            return insets;
        });

        // initialize all buttons
        Button todayButton = findViewById(R.id.today_button);
        Button exerciseButton = findViewById(R.id.exercise_button);

        todayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent todayIntent = new Intent(MainMenu.this, MyExercises.class);

                MainMenu.this.startActivity(todayIntent);
            }
        });

        exerciseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent exerciseIntent = new Intent(MainMenu.this, MyExercises.class);

                MainMenu.this.startActivity(exerciseIntent);
            }
        });
    }
}