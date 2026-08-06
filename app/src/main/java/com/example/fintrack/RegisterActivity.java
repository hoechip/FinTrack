package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edt_m1_full_name;
    private TextInputEditText edt_m1_email;
    private TextInputEditText edt_m1_password;
    private TextInputEditText edt_m1_confirm_password;

    private Button btn_m1_register;
    private TextView txt_m1_login;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        edt_m1_full_name = findViewById(R.id.edt_m1_full_name);
        edt_m1_email = findViewById(R.id.edt_m1_email);
        edt_m1_password = findViewById(R.id.edt_m1_password);
        edt_m1_confirm_password = findViewById(R.id.edt_m1_confirm_password);

        btn_m1_register = findViewById(R.id.btn_m1_register);
        txt_m1_login = findViewById(R.id.txt_m1_login);

        btn_m1_register.setOnClickListener(v -> {

            String name = edt_m1_full_name.getText() != null ? edt_m1_full_name.getText().toString().trim() : "";
            String email = edt_m1_email.getText() != null ? edt_m1_email.getText().toString().trim() : "";
            String pass = edt_m1_password.getText() != null ? edt_m1_password.getText().toString().trim() : "";
            String confirm = edt_m1_confirm_password.getText() != null ? edt_m1_confirm_password.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edt_m1_email.setError("Email không đúng định dạng");
                edt_m1_email.requestFocus();
                return;
            }

            if (pass.length() < 6) {
                edt_m1_password.setError("Mật khẩu phải từ 6 ký tự trở lên");
                edt_m1_password.requestFocus();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            btn_m1_register.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        btn_m1_register.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                            Toast.makeText(RegisterActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                        }
                    });

        });

        txt_m1_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

    }
}