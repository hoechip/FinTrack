package com.example.fintrack;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText edtFullName;
    private TextInputEditText edtBirth;
    private TextInputEditText edtOldPassword;
    private TextInputEditText edtNewPassword;
    private TextInputEditText edtConfirmPassword;

    private TextView txtUserName;
    private TextView txtUserEmail;

    private Button btnSave;

    private ImageButton btnChooseAvatar;
    private ImageView imgAvatar;

    private SharedPreferences sp;

    private FirebaseAuth mAuth;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sp = getSharedPreferences("FinTrack", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        imgAvatar = findViewById(R.id.img_m2_avatar);
        btnChooseAvatar = findViewById(R.id.btn_m2_choose_avatar);

        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);

        edtFullName = findViewById(R.id.edt_m2_full_name);
        edtBirth = findViewById(R.id.edt_m2_birth);

        edtOldPassword = findViewById(R.id.edt_m2_old_password);
        edtNewPassword = findViewById(R.id.edt_m2_new_password);
        edtConfirmPassword = findViewById(R.id.edt_m2_confirm_password);

        btnSave = findViewById(R.id.btn_m2_save);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_profile);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadData();

        txtUserName.setText(sp.getString("FULLNAME", ""));

        txtUserEmail.setText(
                sp.getString("CURRENT_EMAIL",
                        sp.getString("EMAIL", ""))
        );

        pickImageLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri != null) {
                                saveAvatar(uri);
                            }

                        });

        btnChooseAvatar.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        edtBirth.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    String date = String.format("%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year);

                    edtBirth.setText(date);

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();

    }

    private void saveAvatar(Uri uri) {

        try {

            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            File file =
                    new File(getFilesDir(), "avatar.jpg");

            FileOutputStream outputStream =
                    new FileOutputStream(file);

            byte[] buffer = new byte[1024];

            int len;

            while ((len = inputStream.read(buffer)) > 0) {

                outputStream.write(buffer, 0, len);

            }

            outputStream.close();
            inputStream.close();

            imgAvatar.setImageURI(Uri.fromFile(file));

            sp.edit()
                    .putString("AVATAR_PATH",
                            file.getAbsolutePath())
                    .apply();

        } catch (Exception e) {

            Toast.makeText(this,
                    "Không thể lưu ảnh",
                    Toast.LENGTH_SHORT).show();

        }

    }

    private void saveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String birth = edtBirth.getText().toString().trim();
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            edtFullName.setError("Nhập họ tên");
            return;
        }

        com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        // 1. Lưu thông tin hồ sơ lên Firestore (Thay thế SharedPreferences)
        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        java.util.Map<String, Object> profileData = new java.util.HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("birthday", birth);

        firestore.collection("users").document(uid).set(profileData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    // Cập nhật SharedPreferences để đồng bộ local nhanh
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("FULLNAME", fullName);
                    editor.putString("BIRTH", birth);
                    
                    if (!newPass.isEmpty()) {
                        // Logic đổi mật khẩu Firebase
                        user.updatePassword(newPass).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                editor.putString("PASSWORD", newPass);
                                editor.apply();
                                Toast.makeText(this, "Đã đổi mật khẩu và lưu hồ sơ", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Lỗi đổi mật khẩu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        editor.apply();
                        Toast.makeText(this, "Lưu hồ sơ thành công lên Firebase", Toast.LENGTH_SHORT).show();
                    }
                    txtUserName.setText(fullName);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadData() {
        com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        // Ưu tiên load từ Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String birth = documentSnapshot.getString("birthday");
                        String avatarUrl = documentSnapshot.getString("avatarUrl");

                        if (fullName != null) {
                            edtFullName.setText(fullName);
                            txtUserName.setText(fullName);
                            sp.edit().putString("FULLNAME", fullName).apply();
                        }
                        if (birth != null) {
                            edtBirth.setText(birth);
                            sp.edit().putString("BIRTH", birth).apply();
                        }
                        // Xử lý avatarUrl nếu có (trong tương lai)
                    } else {
                        // Fallback về SharedPreferences nếu chưa có trên Firebase
                        edtFullName.setText(sp.getString("FULLNAME", ""));
                        edtBirth.setText(sp.getString("BIRTH", ""));
                    }
                });

        String avatarPath = sp.getString("AVATAR_PATH", "");
        if (!avatarPath.isEmpty()) {
            java.io.File file = new java.io.File(avatarPath);
            if (file.exists()) {
                imgAvatar.setImageURI(android.net.Uri.fromFile(file));
            }
        }
    }

}