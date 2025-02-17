package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(getApplicationContext()));
            }

            return insets;
        });

        // initialize buttons and text fields
        Button loginButton = findViewById(R.id.login_button);
        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (emailInput.getText().toString().equals("user")
                        && passwordInput.getText().toString().equals("password")) {
                    Intent exerciseIntent = new Intent(MainMenu.this, MyExercises.class);

                    MainMenu.this.startActivity(exerciseIntent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Incorrect Email or Password, Please Try Again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

//        todayButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent todayIntent = new Intent(MainMenu.this, MainActivity.class);
//
//                MainMenu.this.startActivity(todayIntent);
//            }
//        });

//        exerciseButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent exerciseIntent = new Intent(MainMenu.this, MyExercises.class);
//
//                MainMenu.this.startActivity(exerciseIntent);
//            }
//        });
    }
}