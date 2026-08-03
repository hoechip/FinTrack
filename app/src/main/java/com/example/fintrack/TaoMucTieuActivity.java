package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class TaoMucTieuActivity extends AppCompatActivity {

    private TextView txt_m11_tieude;

    private EditText edt_m11_ten;
    private EditText edt_m11_sotienmuctieu;
    private EditText edt_m11_sotienhientai;
    private EditText edt_m11_ngayhanchot;

    private Button btn_m11_luu;

    private boolean cheDoSua = false;
    private String mucTieuId; // ID document Firestore, chỉ có giá trị khi cheDoSua = true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tên file XML hiện tại của bạn
        setContentView(R.layout.activity_taomuctieu);

        anhXa();
        nhanDuLieu();
        xuLySuKien();
        chonNgay();
        dinhDangTien(edt_m11_sotienmuctieu);
        dinhDangTien(edt_m11_sotienhientai);
    }

    private void anhXa() {
        txt_m11_tieude = findViewById(R.id.txt_m11_tieude);

        edt_m11_ten =
                findViewById(R.id.edt_m11_ten);

        edt_m11_sotienmuctieu =
                findViewById(R.id.edt_m11_sotienmuctieu);

        edt_m11_sotienhientai =
                findViewById(R.id.edt_m11_sotienhientai);

        edt_m11_ngayhanchot =
                findViewById(R.id.edt_m11_ngayhanchot);

        btn_m11_luu =
                findViewById(R.id.btn_m11_luu);
    }

    private void nhanDuLieu() {
        Intent intent = getIntent();

        cheDoSua = intent.getBooleanExtra(
                "cheDoSua",
                false
        );

        if (cheDoSua) {
            txt_m11_tieude.setText("Chỉnh sửa mục tiêu");
            btn_m11_luu.setText("Cập nhật mục tiêu");

            mucTieuId = intent.getStringExtra("id");

            String tenMucTieu =
                    intent.getStringExtra("tenMucTieu");

            long soTienMucTieu =
                    intent.getLongExtra("soTienMucTieu", 0);

            long soTienHienTai =
                    intent.getLongExtra("soTienHienTai", 0);

            String ngayHanChot =
                    intent.getStringExtra("ngayHanChot");

            edt_m11_ten.setText(tenMucTieu);

            edt_m11_sotienmuctieu.setText(
                    String.valueOf(soTienMucTieu)
            );

            edt_m11_sotienhientai.setText(
                    String.valueOf(soTienHienTai)
            );

            edt_m11_ngayhanchot.setText(ngayHanChot);

        } else {
            txt_m11_tieude.setText("Tạo mục tiêu tiết kiệm");
            btn_m11_luu.setText("Lưu mục tiêu");
        }
    }

    private void xuLySuKien() {
        btn_m11_luu.setOnClickListener(v -> luuMucTieu());
    }
    private void chonNgay() {

        edt_m11_ngayhanchot.setFocusable(false);
        edt_m11_ngayhanchot.setClickable(true);

        edt_m11_ngayhanchot.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int nam = calendar.get(Calendar.YEAR);
            int thang = calendar.get(Calendar.MONTH);
            int ngay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            TaoMucTieuActivity.this,
                            (view, year, month, dayOfMonth) -> {

                                String ngayChon = String.format(
                                        "%02d/%02d/%04d",
                                        dayOfMonth,
                                        month + 1,
                                        year
                                );

                                edt_m11_ngayhanchot.setText(ngayChon);
                                edt_m11_ngayhanchot.setError(null);
                            },
                            nam,
                            thang,
                            ngay
                    );

            // Đặt thời gian về đầu ngày hôm nay
            Calendar ngayHienTai = Calendar.getInstance();
            ngayHienTai.set(Calendar.HOUR_OF_DAY, 0);
            ngayHienTai.set(Calendar.MINUTE, 0);
            ngayHienTai.set(Calendar.SECOND, 0);
            ngayHienTai.set(Calendar.MILLISECOND, 0);

            // Không cho chọn những ngày trước hôm nay
            datePickerDialog.getDatePicker().setMinDate(
                    ngayHienTai.getTimeInMillis()
            );

            datePickerDialog.show();
        });
    }
    private void dinhDangTien(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            private boolean isEditing;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (isEditing) return;

                isEditing = true;

                String text = s.toString().replace(".", "");

                if (!text.isEmpty()) {

                    long value = Long.parseLong(text);

                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setGroupingSeparator('.');

                    DecimalFormat formatter = new DecimalFormat("#,###", symbols);

                    editText.setText(formatter.format(value));
                    editText.setSelection(editText.getText().length());
                }

                isEditing = false;
            }
        });
    }
    private void luuMucTieu() {
        String tenMucTieu =
                edt_m11_ten.getText()
                        .toString()
                        .trim();

        String soTienMucTieuChuoi =
                edt_m11_sotienmuctieu.getText()
                        .toString()
                        .trim();

        String soTienHienTaiChuoi =
                edt_m11_sotienhientai.getText()
                        .toString()
                        .trim();

        String ngayHanChot =
                edt_m11_ngayhanchot.getText()
                        .toString()
                        .trim();

        // Kiểm tra tên mục tiêu
        if (tenMucTieu.isEmpty()) {
            edt_m11_ten.setError(
                    "Vui lòng nhập tên mục tiêu"
            );
            edt_m11_ten.requestFocus();
            return;
        }

        // Kiểm tra số tiền mục tiêu
        if (soTienMucTieuChuoi.isEmpty()) {
            edt_m11_sotienmuctieu.setError(
                    "Vui lòng nhập số tiền mục tiêu"
            );
            edt_m11_sotienmuctieu.requestFocus();
            return;
        }

        // Kiểm tra số tiền hiện tại
        if (soTienHienTaiChuoi.isEmpty()) {
            edt_m11_sotienhientai.setError(
                    "Vui lòng nhập số tiền hiện tại"
            );
            edt_m11_sotienhientai.requestFocus();
            return;
        }

        // Kiểm tra ngày hạn chót
        if (ngayHanChot.isEmpty()) {
            edt_m11_ngayhanchot.setError(
                    "Vui lòng nhập ngày hạn chót"
            );
            edt_m11_ngayhanchot.requestFocus();
            return;
        }

        long soTienMucTieu;
        long soTienHienTai;

        try {
            soTienMucTieu =
                    Long.parseLong(
                            soTienMucTieuChuoi.replace(".", "")
                    );

            soTienHienTai =
                    Long.parseLong(
                            soTienHienTaiChuoi.replace(".", "")
                    );

        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Số tiền không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (soTienMucTieu <= 0) {
            edt_m11_sotienmuctieu.setError(
                    "Số tiền mục tiêu phải lớn hơn 0"
            );
            edt_m11_sotienmuctieu.requestFocus();
            return;
        }

        if (soTienHienTai < 0) {
            edt_m11_sotienhientai.setError(
                    "Số tiền hiện tại không được nhỏ hơn 0"
            );
            edt_m11_sotienhientai.requestFocus();
            return;
        }

        Intent resultIntent = new Intent();

        if (cheDoSua) {
            resultIntent.putExtra("id", mucTieuId);
        }

        resultIntent.putExtra(
                "tenMucTieu",
                tenMucTieu
        );

        resultIntent.putExtra(
                "soTienMucTieu",
                soTienMucTieu
        );

        resultIntent.putExtra(
                "soTienHienTai",
                soTienHienTai
        );

        resultIntent.putExtra(
                "ngayHanChot",
                ngayHanChot
        );

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}