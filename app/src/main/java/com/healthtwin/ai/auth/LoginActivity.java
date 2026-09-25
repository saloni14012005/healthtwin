
        package com.healthtwin.ai.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.healthtwin.ai.R;
import com.healthtwin.ai.dashboard.DashboardActivity;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogin;
    TextView tvSignup, tvForgotPassword;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Login
        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Enter Email");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Enter Password");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    DashboardActivity.class
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        // Signup
        tvSignup.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            LoginActivity.this,
                            SignupActivity.class
                    )
            );
        });

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Enter your email first");
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Password reset link sent",
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }

    // Check if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            Intent intent = new Intent(
                    LoginActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);
            finish();
        }
    }
}