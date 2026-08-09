package com.example.fintrack;

import android.os.Bundle;
import android.util.Patterns;
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

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edt_m4_email);
        btnSendOTP = findViewById(R.id.btn_m4_send_otp);

        btnSendOTP.setOnClickListener(v -> {

            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";

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

            btnSendOTP.setEnabled(false);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        btnSendOTP.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Đã gửi email khôi phục mật khẩu. Vui lòng kiểm tra hộp thư!",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        } else {
                            String errorMsg = task.getException() != null ?
                                    task.getException().getMessage() : "Gửi thất bại";

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Lỗi: " + errorMsg,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

        });

    }
}