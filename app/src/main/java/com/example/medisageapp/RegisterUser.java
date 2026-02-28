package com.example.medisageapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterUser extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextPassword;
    private Button registerBtn;
    private FirebaseAuth mAuth;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        editTextFullName = findViewById(R.id.fullName);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        loginRedirect = findViewById(R.id.loginRedirect);

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This takes the user back to the Login activity
                // Replace 'LoginActivity.class' with the actual name of your login file
                Intent intent = new Intent(RegisterUser.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optional: finish current activity so they don't come back here on 'Back' press
            }
        });
    }

    private void registerNewUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();

        // 1. Full Name Validation
        if (fullName.isEmpty()) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }

        // 2. Email Validation
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please provide a valid email");
            editTextEmail.requestFocus();
            return;
        }

        // 3. 🛡️ Strong Password Validation
        // Constraints: 1 Upper, 1 Lower, 1 Digit, 1 Special Char, No Spaces, Min 6 chars
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$";

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.matches(passwordPattern)) {
            editTextPassword.setError("Password must contain at least one uppercase letter, one number, and one special character (@#$%^&+=!)");
            editTextPassword.requestFocus();
            return;
        }

        // 4. Firebase Registration Logic
        // Only runs if all validations above pass
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterUser.this, "Welcome to Medisage! Account created.", Toast.LENGTH_LONG).show();
                        // Close activity and return to Login
                        finish();
                    } else {
                        // Check for common errors (like email already exists)
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterUser.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}