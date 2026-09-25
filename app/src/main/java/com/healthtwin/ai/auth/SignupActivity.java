package com.healthtwin.ai.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.FirebaseAuth;

import com.healthtwin.ai.R;


public class SignupActivity extends AppCompatActivity {


    TextInputEditText etName, etEmail, etPassword, etConfirmPassword;

    MaterialButton btnSignup;

    TextView tvLogin;

    FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);



        // Firebase initialization
        auth = FirebaseAuth.getInstance();



        // XML Connections

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignup = findViewById(R.id.btnSignup);

        tvLogin = findViewById(R.id.tvLogin);




        // Signup Button Click

        btnSignup.setOnClickListener(v -> {


            String name = etName.getText().toString().trim();

            String email = etEmail.getText().toString().trim();

            String password = etPassword.getText().toString().trim();

            String confirmPassword = etConfirmPassword.getText().toString().trim();




            // Validation

            if(name.isEmpty()){

                etName.setError("Enter your name");

            }
            else if(name.length() < 3){

                etName.setError("Name must be at least 3 characters");

            }
            else if(!name.matches("[a-zA-Z ]+")){

                etName.setError("Only alphabets are allowed");

            }
            else if(email.isEmpty()){

                etEmail.setError("Enter your email");

            }
            else if(password.isEmpty()){

                etPassword.setError("Enter password");

            }
            else if(password.length() < 6){

                etPassword.setError("Password must be at least 6 characters");

            }
            else if(!password.equals(confirmPassword)){

                etConfirmPassword.setError("Password does not match");

            }
            else{

                createAccount(email,password);

            }


        });   // ✅ Signup button listener closed here




        // Go to Login

        tvLogin.setOnClickListener(v -> {


            Intent intent = new Intent(
                    SignupActivity.this,
                    LoginActivity.class
            );


            startActivity(intent);

            finish();


        });


    }





    // Firebase Account Creation Method

    private void createAccount(String email, String password) {


        auth.createUserWithEmailAndPassword(email,password)

                .addOnCompleteListener(task -> {



                    if(task.isSuccessful()){


                        Toast.makeText(
                                SignupActivity.this,
                                "Account Created Successfully",
                                Toast.LENGTH_LONG
                        ).show();



                        Intent intent = new Intent(
                                SignupActivity.this,
                                LoginActivity.class
                        );


                        startActivity(intent);

                        finish();


                    }
                    else{


                        Toast.makeText(
                                SignupActivity.this,
                                "Signup Failed: "
                                        + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();


                    }



                });


    }


}