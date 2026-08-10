package com.example.fintrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edt_m1_full_name;
    private TextInputEditText edt_m1_email;
    private TextInputEditText edt_m1_password;
    private TextInputEditText edt_m1_confirm_password;

    private Button btn_m1_register;
    private TextView txt_m1_login;
    private com.google.firebase.auth.FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        edt_m1_full_name = findViewById(R.id.edt_m1_full_name);
        edt_m1_email = findViewById(R.id.edt_m1_email);
        edt_m1_password = findViewById(R.id.edt_m1_password);
        edt_m1_confirm_password = findViewById(R.id.edt_m1_confirm_password);

        btn_m1_register = findViewById(R.id.btn_m1_register);
        txt_m1_login = findViewById(R.id.txt_m1_login);

        btn_m1_register.setOnClickListener(v -> {

            String name = edt_m1_full_name.getText().toString().trim();
            String email = edt_m1_email.getText().toString().trim();
            String pass = edt_m1_password.getText().toString(); // Không dùng trim() cho mật khẩu
            String confirm = edt_m1_confirm_password.getText().toString();

            if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()){
                Toast.makeText(this,"Nhập đầy đủ thông tin",Toast.LENGTH_SHORT).show();
                return;
            }

            if(!pass.equals(confirm)){
                Toast.makeText(this,"Mật khẩu xác nhận không đúng",Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Xóa dữ liệu cũ của tài khoản trước đó trên máy này
                            try {
                                DatabaseHelper dbHelper = new DatabaseHelper(this);
                                dbHelper.clearAllData();

                                SharedPreferences sp = getSharedPreferences("FinTrack", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.clear(); // Làm mới hoàn toàn SharedPreferences

                                editor.putString("FULLNAME", name);
                                editor.putString("EMAIL", email);
                                editor.putString("PASSWORD", pass);
                                editor.apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Chuyển sang màn hình đăng nhập ngay, không đợi Google Drive signOut (tránh treo)
                            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            
                            // Thực hiện signOut Google Drive ở background nếu có thể
                            try {
                                new com.example.fintrack.util.GoogleDriveHelper(this).signOut(() -> {
                                    // Đã thoát Google Drive cũ
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        }
                    });

        });

        txt_m1_login.setOnClickListener(v->{
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        });

    }
}