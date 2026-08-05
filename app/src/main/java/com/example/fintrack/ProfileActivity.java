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
    private ImageButton btnBack;

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
        btnBack = findViewById(R.id.btn_profile_back);

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

        btnBack.setOnClickListener(v -> finish());

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

        String fullName =
                edtFullName.getText().toString().trim();

        String birth =
                edtBirth.getText().toString().trim();

        String oldPass =
                edtOldPassword.getText().toString().trim();

        String newPass =
                edtNewPassword.getText().toString().trim();

        String confirmPass =
                edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {

            edtFullName.setError("Nhập họ tên");

            return;

        }

        if (birth.isEmpty()) {

            edtBirth.setError("Chọn ngày sinh");

            return;

        }

        SharedPreferences.Editor editor = sp.edit();

        editor.putString("FULLNAME", fullName);
        editor.putString("BIRTH", birth);

        if (!newPass.isEmpty()) {

            String currentPassword =
                    sp.getString("PASSWORD", "");

            if (!oldPass.equals(currentPassword)) {

                edtOldPassword.setError("Sai mật khẩu cũ");

                return;

            }

            if (!newPass.equals(confirmPass)) {

                edtConfirmPassword.setError("Mật khẩu không khớp");

                return;

            }

            editor.putString("PASSWORD", newPass);

        }

        editor.apply();

        txtUserName.setText(fullName);

        Toast.makeText(this,
                "Lưu thay đổi thành công",
                Toast.LENGTH_SHORT).show();

        edtOldPassword.setText("");
        edtNewPassword.setText("");
        edtConfirmPassword.setText("");

    }

    private void loadData() {

        edtFullName.setText(
                sp.getString("FULLNAME", ""));

        edtBirth.setText(
                sp.getString("BIRTH", ""));

        String avatarPath =
                sp.getString("AVATAR_PATH", "");

        if (!avatarPath.isEmpty()) {

            File file = new File(avatarPath);

            if (file.exists()) {

                imgAvatar.setImageURI(Uri.fromFile(file));

            }

        }

    }

}