package com.assignment.androidshoppingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.androidshoppingapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editUsername, editPassword;
    private Button saveButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Khởi tạo các view
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.backButton);

        // Hiển thị dữ liệu hiện tại
        Intent intent = getIntent();
        editName.setText(intent.getStringExtra("name"));
        editEmail.setText(intent.getStringExtra("email"));
        editUsername.setText(intent.getStringExtra("username"));
        editPassword.setText(intent.getStringExtra("password"));

        // Xử lý nút Back
        backButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // Thông báo không có thay đổi
            finish(); // Quay lại mà không lưu
        });

        // Xử lý nút Save
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void saveUserData() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User").child(username);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("username", username);
        updates.put("password", password);

        reference.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                setResult(RESULT_OK, intent); // Trả kết quả thành công
                finish(); // Quay lại ProfileActivity
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}