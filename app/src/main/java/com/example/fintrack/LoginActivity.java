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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

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
            String password = edtPassword.getText().toString().trim();

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

            // Lấy dữ liệu đã đăng ký
            SharedPreferences sp = getSharedPreferences("FinTrack", MODE_PRIVATE);

            String savedEmail = sp.getString("EMAIL", "");
            String savedPassword = sp.getString("PASSWORD", "");

            // Kiểm tra tài khoản
            if (email.equals(savedEmail) && password.equals(savedPassword)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("CURRENT_EMAIL", email);
                editor.apply();
                Toast.makeText(
                        LoginActivity.this,
                        "Đăng nhập thành công",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        LoginActivity.this,
                        MainActivity.class
                );

                startActivity(intent);
                finish();

            } else {

                Toast.makeText(
                        LoginActivity.this,
                        "Sai Email hoặc mật khẩu",
                        Toast.LENGTH_SHORT
                ).show();
            }

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