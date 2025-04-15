package com.example.physiokneeds_v3;

import static com.example.physiokneeds_v3.HomeScreen.TAG_API;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainMenu extends AppCompatActivity {

    String authCode = "";
    public static String tokenId = "";
    public static String email = "";
    public static String name = "";

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

        Button googleButton = findViewById(R.id.login_google_button);

        // ask for permission on start up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            get_permissions();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (emailInput.getText().toString().equals("user")
                        && passwordInput.getText().toString().equals("password")) {
//                    Intent homeIntent = new Intent(MainMenu.this, HomeScreen.class);
//                    homeIntent.putExtra(USERNAME_TAG, emailInput.getText().toString());
//
//                    MainMenu.this.startActivity(homeIntent);
                    Toast.makeText(getApplicationContext(),
                            "Please sign in with Google",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Incorrect Email or Password, Please Try Again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startGoogleSignIn();
            }
        });
    }

    private void startGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode("825627223283-7he30709s5prqngspqcskquq4bf8ud4t.apps.googleusercontent.com", true)
                .requestEmail()
                .requestProfile()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);

//        googleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
//            Intent signInIntent = googleSignInClient.getSignInIntent();
//            startActivityForResult(signInIntent, 101);
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    authCode = account.getServerAuthCode();
                    email = account.getEmail();
                    name = account.getDisplayName();

                    loadData();
                }

            } catch (ApiException e) {
                Log.e("GOOGLE_SIGN_IN", "Sign-in failed", e);
            }
        }
    }

    void loadData() {
        // create retrofit instance for both routine configs and routine data
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://140.238.151.117:8000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<Token> callTokenId = apiService.getTokenId(authCode);

        if (callTokenId != null) {
            callTokenId.enqueue(new Callback<Token>() {
                @Override
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if (!response.isSuccessful()) {
                        // Handle the error scenario here
                        Log.e(TAG_API, "Response Code: " + response.code());
                        Log.d(TAG_API, call.request().url().toString());
                        return;
                    }

                    tokenId = "Token " + response.body().getToken();
                    Log.d(TAG_API, tokenId);

                    Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<Token> call, Throwable t) {
                    Log.e(TAG_API, t.toString());
                }
            });
        }

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