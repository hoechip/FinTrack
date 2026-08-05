package com.example.fintrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView tvAmount;
    private LinearLayout llEnvelopes;
    private DatabaseHelper dbHelper;
    private FirebaseHelper firebaseHelper;
    private final StringBuilder currentInput = new StringBuilder();
    private String selectedEnvelopeId = "-1";
    private String selectedEnvelopeName = "";
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);
        firebaseHelper = FirebaseHelper.layThucThe();

        tvAmount = findViewById(R.id.tvAmount);
        llEnvelopes = findViewById(R.id.llEnvelopes);
        GridLayout glCalculator = findViewById(R.id.glCalculator);
        Button btnSave = findViewById(R.id.btnSave);
        View btnBack = findViewById(R.id.btnBack);
        View btnGoToHistory = findViewById(R.id.btnGoToHistory);

        if (llEnvelopes != null) {
            loadEnvelopes();
        }
        if (glCalculator != null) {
            setupCalculator(glCalculator);
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveTransaction());
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (btnGoToHistory != null) {
            btnGoToHistory.setOnClickListener(v -> finish());
        }

        updateAmountDisplay();
    }

    private void loadEnvelopes() {
        KeHoachNganSach keHoach = firebaseHelper.layKeHoachHienTai();
        llEnvelopes.removeAllViews();
        
        if (keHoach != null && keHoach.getDanhSachPhongBi() != null) {
            for (PhongBi pb : keHoach.getDanhSachPhongBi()) {
                String id = pb.getId();
                String name = pb.getTenHangMuc();
                String iconTag = pb.getLoaiIcon();
                long remaining = pb.getSoDuConLai();

                // Map icon tag to emoji/string for display in circular button
                String iconStr = "👛"; // Default
                if (iconTag != null) {
                    switch (iconTag) {
                        case "food": iconStr = "🍔"; break;
                        case "house": iconStr = "🏠"; break;
                        case "bus": iconStr = "🚗"; break;
                        case "lightning": iconStr = "💡"; break;
                        case "gamepad": iconStr = "🎮"; break;
                        case "shopping": iconStr = "🛍️"; break;
                    }
                }

                LinearLayout itemLayout = createEnvelopeView(id, name, iconStr, remaining);
                llEnvelopes.addView(itemLayout);
            }
        }
    }

    private LinearLayout createEnvelopeView(String id, String name, String icon, double remaining) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        float scale = getResources().getDisplayMetrics().density;
        int paddingSide = (int) (10 * scale + 0.5f);
        int paddingTopBottom = (int) (6 * scale + 0.5f);

        container.setPadding(paddingSide, paddingTopBottom, paddingSide, paddingTopBottom);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, (int) (8 * scale + 0.5f), 0);
        container.setLayoutParams(params);
        container.setMinimumWidth((int) (70 * scale + 0.5f));

        setEnvelopeBackground(container, Objects.equals(id, selectedEnvelopeId));

        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(18);
        container.addView(tvIcon);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#333333"));
        tvName.setTextSize(11);
        tvName.setPadding(0, 4, 0, 2);
        tvName.setGravity(Gravity.CENTER);
        tvName.setSingleLine(true);
        container.addView(tvName);

        TextView tvBalance = new TextView(this);
        String balanceStr = formatter.format(remaining) + " đ";
        tvBalance.setText(balanceStr);
        tvBalance.setTextColor(Color.parseColor("#888888"));
        tvBalance.setTextSize(8);
        tvBalance.setGravity(Gravity.CENTER);
        tvBalance.setSingleLine(true);
        container.addView(tvBalance);

        container.setOnClickListener(v -> {
            selectedEnvelopeId = id;
            selectedEnvelopeName = name;

            for (int i = 0; i < llEnvelopes.getChildCount(); i++) {
                setEnvelopeBackground((LinearLayout) llEnvelopes.getChildAt(i), false);
            }
            setEnvelopeBackground(container, true);
        });

        return container;
    }

    private void setEnvelopeBackground(LinearLayout layout, boolean selected) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24);
        if (selected) {
            shape.setColor(Color.parseColor("#F3F4F6"));
            shape.setStroke(3, Color.parseColor("#D1D5DB"));
        } else {
            shape.setColor(Color.WHITE);
            shape.setStroke(1, Color.parseColor("#EEEEEE"));
        }
        layout.setBackground(shape);
    }

    private void setupCalculator(GridLayout gl) {
        for (int i = 0; i < gl.getChildCount(); i++) {
            View child = gl.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                btn.setAllCaps(false);
                btn.setOnClickListener(v -> onCalcButtonClick(btn.getText().toString()));
            }
        }
    }

    private void onCalcButtonClick(String text) {
        if (Objects.equals(text, "⌫")) {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
        } else if (Objects.equals(text, "C")) {
            currentInput.setLength(0);
        } else if (Objects.equals(text, "=")) {
            evaluateExpression();
        } else if (Objects.equals(text, "000")) {
             if (currentInput.length() > 0 && !isOperator(currentInput.charAt(currentInput.length() - 1))) {
                currentInput.append("000");
            }
        } else {
            if (isOperator(text.charAt(0)) && currentInput.length() > 0) {
                char last = currentInput.charAt(currentInput.length() - 1);
                if (isOperator(last)) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                }
            }
            currentInput.append(text);
        }
        updateAmountDisplay();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private void updateAmountDisplay() {
        if (tvAmount == null) return;
        String val = currentInput.length() == 0 ? "0" : currentInput.toString();
        if (!containsOperator(val)) {
            try {
                double d = Double.parseDouble(val);
                tvAmount.setText("-" + formatter.format(d));
            } catch (Exception e) {
                tvAmount.setText("-" + val);
            }
        } else {
            tvAmount.setText("-" + val);
        }
    }

    private boolean containsOperator(String s) {
        return s.contains("+") || s.contains("-") || s.contains("*") || s.contains("/");
    }

    private void evaluateExpression() {
        try {
            String expr = currentInput.toString();
            if (expr.isEmpty()) return;

            double result = 0;
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+");
                for (String p : parts) if (!p.isEmpty()) result += Double.parseDouble(p);
                currentInput.setLength(0);
                currentInput.append((long) result);
            } else if (expr.contains("-")) {
                String[] parts = expr.split("-");
                result = Double.parseDouble(parts[0]);
                for (int i = 1; i < parts.length; i++) if (!parts[i].isEmpty()) result -= Double.parseDouble(parts[i]);
                currentInput.setLength(0);
                currentInput.append((long) result);
            }
        } catch (Exception ignored) {
        }
    }

    private void saveTransaction() {
        evaluateExpression();
        String amountStr = currentInput.toString();

        if (amountStr.isEmpty() || Objects.equals(selectedEnvelopeId, "-1") || amountStr.equals("0")) {
            Toast.makeText(this, "Vui lòng nhập số tiền và chọn phong bì", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = (long) Double.parseDouble(amountStr);

        // 1. Cập nhật SQLite để lưu lịch sử giao dịch (Hiển thị ở Lịch sử)
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = sdf.format(new Date());

            ContentValues transValues = new ContentValues();
            transValues.put(DatabaseHelper.COLUMN_TRANS_TITLE, "Chi tiêu " + selectedEnvelopeName);
            transValues.put(DatabaseHelper.COLUMN_TRANS_AMOUNT, (double) amount);
            transValues.put(DatabaseHelper.COLUMN_TRANS_ENV_ID, selectedEnvelopeId); // Lưu ID phong bì
            transValues.put(DatabaseHelper.COLUMN_TRANS_DATE, currentDate);
            db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, transValues);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // 2. Cập nhật Firebase để đồng bộ với Trang chủ và Hạn mức trực quan
        KeHoachNganSach keHoach = firebaseHelper.layKeHoachHienTai();
        if (keHoach != null && keHoach.getDanhSachPhongBi() != null) {
            for (PhongBi pb : keHoach.getDanhSachPhongBi()) {
                if (Objects.equals(pb.getId(), selectedEnvelopeId)) {
                    long currentSpent = pb.getDaTieu();
                    pb.setDaTieu(currentSpent + amount);
                    break;
                }
            }
            
            firebaseHelper.luuKeHoachNganSach(keHoach, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddTransactionActivity.this, "Đã cập nhật chi tiêu vào " + selectedEnvelopeName, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Lỗi đồng bộ Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
