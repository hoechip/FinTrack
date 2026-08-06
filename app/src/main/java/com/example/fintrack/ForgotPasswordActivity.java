package com.example.fintrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private Button btnSendOTP;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_forgot_password);
        toolbar.setNavigationOnClickListener(v -> finish());

        edtEmail = findViewById(R.id.edt_m4_email);
        btnSendOTP = findViewById(R.id.btn_m4_send_otp);

        mAuth = FirebaseAuth.getInstance();

        btnSendOTP.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Nhập Email");
                edtEmail.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Đã gửi email đặt lại mật khẩu",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();

                        } else {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                        }

                    });

        });

    }
}