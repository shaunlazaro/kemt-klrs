package com.example.physiokneeds_v3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainMenu extends AppCompatActivity {

    // intent sending constant
    public static final String USERNAME_TAG = "USERNAME";

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

        // ask for permission on start up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            get_permissions();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if (emailInput.getText().toString().equals("user")
//                        && passwordInput.getText().toString().equals("password")) {
                    Intent homeIntent = new Intent(MainMenu.this, HomeScreen.class);
                    homeIntent.putExtra(USERNAME_TAG, emailInput.getText().toString());

                    MainMenu.this.startActivity(homeIntent);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "Incorrect Email or Password, Please Try Again",
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    void get_permissions() {

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH_CONNECT};

        boolean cameraTrue = false;
        boolean btTrue = false;

        // camera permissions
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            cameraTrue = true;
        }

        // bluetooth permissions
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            btTrue = true;
        }

        if (cameraTrue && btTrue) {
            requestPermissions(permissions, 101);
        } else if (cameraTrue) {
            requestPermissions(new String[]{(Manifest.permission.CAMERA)}, 101);
        } else if (btTrue) {
            requestPermissions(new String[]{(Manifest.permission.BLUETOOTH_CONNECT)}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                get_permissions(); // Call the method to request permissions again
            }
        }
    }
}