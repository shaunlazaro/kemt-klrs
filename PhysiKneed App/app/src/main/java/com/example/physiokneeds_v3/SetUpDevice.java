package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SetUpDevice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_up_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button nextButton = findViewById(R.id.next_button);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton backButton = findViewById(R.id.back_button_blue);

        DisplayManager displayManager = (DisplayManager) getSystemService(this.DISPLAY_SERVICE);
        if (displayManager.getDisplays().length >= 2) {
            nextButton.setEnabled(true);
        }

        RoutineConfig routineConfig = (RoutineConfig) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                boolean isCasted = getIntent().getBooleanExtra(MyExercises.IS_CASTED, false);

                if (isCasted) {
                    Intent intent = new Intent(SetUpDevice.this, PerfromExercises.class);
                    intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig);
                    SetUpDevice.this.startActivity(intent);
                } else {
                    Intent intent = new Intent(SetUpDevice.this, MainActivity.class);
                    SetUpDevice.this.startActivity(intent);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SetUpDevice.this, MyExercises.class);

                SetUpDevice.this.startActivity(intent);
            }
        });
    }
}