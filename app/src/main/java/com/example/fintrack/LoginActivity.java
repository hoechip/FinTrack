package com.example.fintrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister, txtForgot;
    private com.google.firebase.auth.FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        // ========================
        // Kiểm tra Firebase
        // ========================
        FirebaseApp app = FirebaseApp.getInstance();
        Log.d("FirebaseTest", "Firebase = " + app.getName());

        // ========================
        // Ánh xạ View
        // ========================
        edtEmail = findViewById(R.id.edt_m1_email);
        edtPassword = findViewById(R.id.edt_m1_password);

        btnLogin = findViewById(R.id.btn_m1_login);

        txtRegister = findViewById(R.id.txt_m1_register);
        txtForgot = findViewById(R.id.txt_m1_forgot_password);

        // ========================
        // Đăng nhập
        // ========================
        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString(); // Không dùng trim() cho mật khẩu

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập Email");
                edtEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Xóa dữ liệu cũ của tài khoản trước đó trên máy này
                            DatabaseHelper dbHelper = new DatabaseHelper(this);
                            dbHelper.clearAllData();

                            // Đăng nhập thành công, cập nhật SharedPreferences để đồng bộ local
                            SharedPreferences sp = getSharedPreferences("FinTrack", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.clear(); // Làm mới dữ liệu cũ của tài khoản khác

                            editor.putString("CURRENT_EMAIL", email);
                            editor.putString("EMAIL", email);
                            editor.putString("PASSWORD", password); // Cập nhật mật khẩu mới vào local
                            editor.apply();

                            // 3. Xóa luôn liên kết Google Drive cũ (nếu có) để nick mới không bị lẫn
                            new com.example.fintrack.util.GoogleDriveHelper(this).signOut(() -> {
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            });
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Sai Email hoặc mật khẩu";
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });

        });

        // ========================
        // Đăng ký
        // ========================
        txtRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);

        });

        // ========================
        // Quên mật khẩu
        // ========================
        txtForgot.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );

            startActivity(intent);

        });

    }
}