package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister, txtForgot;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edt_m1_email);
        edtPassword = findViewById(R.id.edt_m1_password);
        btnLogin = findViewById(R.id.btn_m1_login);
        txtRegister = findViewById(R.id.txt_m1_register);
        txtForgot = findViewById(R.id.txt_m1_forgot_password);

        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập Email");
                edtEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không đúng định dạng");
                edtEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            btnLogin.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        btnLogin.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Sai Email hoặc mật khẩu";
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });

        });

        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        txtForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

    }
}