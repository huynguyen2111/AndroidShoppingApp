package com.assignment.androidshoppingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.assignment.androidshoppingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText resetUsername, resetNewPassword, resetConfirmPassword;
    private Button resetButton;
    private TextView backToLoginText;
    private TextView resetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        resetUsername = findViewById(R.id.reset_username);
        resetNewPassword = findViewById(R.id.reset_new_password);
        resetConfirmPassword = findViewById(R.id.reset_confirm_password);
        resetButton = findViewById(R.id.reset_button);
        backToLoginText = findViewById(R.id.backToLoginText);
        resetPassword = findViewById(R.id.reset_pass);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        backToLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optional: closes ResetPasswordActivity
            }
        });
    }

    private void resetPassword() {
        String userUsername = resetUsername.getText().toString().trim();
        String newPassword = resetNewPassword.getText().toString().trim();
        String confirmPassword = resetConfirmPassword.getText().toString().trim();

        if (userUsername.isEmpty()) {
            resetUsername.setError("Please enter username");
            resetUsername.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            resetNewPassword.setError("Please enter new password");
            resetNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            resetConfirmPassword.setError("Passwords do not match");
            resetConfirmPassword.requestFocus();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    resetUsername.setError(null);

                    // Update password in Firebase
                    reference.child(userUsername).child("password").setValue(newPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    showResetSuccessMessage();
                                } else {
                                    resetUsername.setError("Failed to reset password");
                                }
                            });
                } else {
                    resetUsername.setError("User does not exist");
                    resetUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                resetUsername.setError("Database error occurred");
            }
        });
    }

    private void showResetSuccessMessage() {
        resetUsername.setError(null);
        resetNewPassword.setError(null);
        resetConfirmPassword.setError(null);
//        resetUsername.setText("Password reset successful! Please login with your new password.");
//        resetNewPassword.setText("");
//        resetConfirmPassword.setText("");
        resetPassword.setText("Password reset successful! Please login with your new password.");
    }
}