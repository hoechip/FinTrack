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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edt_m1_full_name = findViewById(R.id.edt_m1_full_name);
        edt_m1_email = findViewById(R.id.edt_m1_email);
        edt_m1_password = findViewById(R.id.edt_m1_password);
        edt_m1_confirm_password = findViewById(R.id.edt_m1_confirm_password);

        btn_m1_register = findViewById(R.id.btn_m1_register);
        txt_m1_login = findViewById(R.id.txt_m1_login);

        btn_m1_register.setOnClickListener(v -> {

            String name = edt_m1_full_name.getText().toString().trim();
            String email = edt_m1_email.getText().toString().trim();
            String pass = edt_m1_password.getText().toString().trim();
            String confirm = edt_m1_confirm_password.getText().toString().trim();

            if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()){
                Toast.makeText(this,"Nhập đầy đủ thông tin",Toast.LENGTH_SHORT).show();
                return;
            }

            if(!pass.equals(confirm)){
                Toast.makeText(this,"Mật khẩu xác nhận không đúng",Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("FinTrack",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString("FULLNAME",name);
            editor.putString("EMAIL",email);
            editor.putString("PASSWORD",pass);
            editor.apply();

            Toast.makeText(this,"Đăng ký thành công",Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this,LoginActivity.class));
            finish();

        });

        txt_m1_login.setOnClickListener(v->{
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        });

    }
}