package com.assignment.androidshoppingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail, profileUsername, profilePassword;
    private TextView titleName, titleUsername;
    private Button editProfile;
    private ImageView backButton;
    private static final int EDIT_PROFILE_REQUEST = 1; // Mã request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo các view
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileUsername = findViewById(R.id.profileUsername);
        titleName = findViewById(R.id.titleName);
        titleUsername = findViewById(R.id.titleUsername);
        editProfile = findViewById(R.id.editButton);
        backButton = findViewById(R.id.backButton);

        // Hiển thị dữ liệu người dùng từ Firebase ngay khi mở
        loadUserDataFromFirebase();

        // Xử lý nút Back
        backButton.setOnClickListener(v -> finish());

        // Xử lý nút Edit Profile
        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("name", profileName.getText().toString());
            intent.putExtra("email", profileEmail.getText().toString());
            intent.putExtra("username", profileUsername.getText().toString());
//            intent.putExtra("password", profilePassword.getText().toString());
            startActivityForResult(intent, EDIT_PROFILE_REQUEST); // Mở EditProfileActivity với request code
        });
    }

    private void loadUserDataFromFirebase() {
        Intent intent = getIntent();
        String usernameUser = intent.getStringExtra("username");

        if (usernameUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
            Query checkUserDatabase = reference.orderByChild("username").equalTo(usernameUser);

            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameFromDB = snapshot.child(usernameUser).child("name").getValue(String.class);
                        String emailFromDB = snapshot.child(usernameUser).child("email").getValue(String.class);
                        String usernameFromDB = snapshot.child(usernameUser).child("username").getValue(String.class);
//                        String passwordFromDB = snapshot.child(usernameUser).child("password").getValue(String.class);

                        // Cập nhật giao diện
                        titleName.setText(nameFromDB);
                        titleUsername.setText(usernameFromDB);
                        profileName.setText(nameFromDB);
                        profileEmail.setText(emailFromDB);
                        profileUsername.setText(usernameFromDB);
//                        profilePassword.setText(passwordFromDB);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu cần
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST) {
            // Tải lại dữ liệu từ Firebase bất kể Save hay Back
            loadUserDataFromFirebase();
        }
    }
}