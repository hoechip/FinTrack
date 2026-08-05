package com.example.themgd_tracuu;

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

import java.text.DecimalFormat;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView tvAmount;
    private LinearLayout llEnvelopes;
    private DatabaseHelper dbHelper;
    private final StringBuilder currentInput = new StringBuilder();
    private long selectedEnvelopeId = -1;
    private String selectedEnvelopeName = "";
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        tvAmount = findViewById(R.id.tvAmount);
        llEnvelopes = findViewById(R.id.llEnvelopes);
        GridLayout glCalculator = findViewById(R.id.glCalculator);
        Button btnSave = findViewById(R.id.btnSave);
        View btnBack = findViewById(R.id.btnBack);
        View btnGoToSearch = findViewById(R.id.btnGoToHistory);

        loadEnvelopes();
        setupCalculator(glCalculator);

        btnSave.setOnClickListener(v -> saveTransaction());
        btnBack.setOnClickListener(v -> finish());
        btnGoToSearch.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, SearchActivity.class);
            startActivity(intent);
        });
        updateAmountDisplay();
    }

    private void loadEnvelopes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ENVELOPES, null, null, null, null, null, null);

        llEnvelopes.removeAllViews();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_NAME));
            String icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_ICON));
            double remaining = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_REMAINING));

            LinearLayout itemLayout = createEnvelopeView(id, name, icon, remaining);
            llEnvelopes.addView(itemLayout);
        }
        cursor.close();
    }

    private LinearLayout createEnvelopeView(long id, String name, String icon, double remaining) {
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

        setEnvelopeBackground(container, id == selectedEnvelopeId);

        // Icon
        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(18); 
        container.addView(tvIcon);

        // Name
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#333333"));
        tvName.setTextSize(11);
        tvName.setPadding(0, 4, 0, 2);
        tvName.setGravity(Gravity.CENTER);
        tvName.setSingleLine(true);
        container.addView(tvName);

        // Balance
        TextView tvBalance = new TextView(this);
        String balanceStr = "-" + formatter.format(remaining) + " đ";
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
            if (currentInput.length() > 0 && !isOperator(currentInput.charAt(currentInput.length()-1))) {
                currentInput.append("000");
            }
        } else {
            // Prevent consecutive operators
            if (isOperator(text.charAt(0)) && currentInput.length() > 0) {
                char last = currentInput.charAt(currentInput.length()-1);
                if (isOperator(last)) {
                    currentInput.deleteCharAt(currentInput.length()-1);
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
        String val = currentInput.length() == 0 ? "0" : currentInput.toString();
        // If it's a simple number, format it
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
            
            // Basic support for addition/subtraction for "cộng dồn"
            double result = 0;
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+");
                for (String p : parts) if (!p.isEmpty()) result += Double.parseDouble(p);
                currentInput.setLength(0);
                currentInput.append((long)result);
            } else if (expr.contains("-")) {
                String[] parts = expr.split("-");
                result = Double.parseDouble(parts[0]);
                for (int i = 1; i < parts.length; i++) if (!parts[i].isEmpty()) result -= Double.parseDouble(parts[i]);
                currentInput.setLength(0);
                currentInput.append((long)result);
            }
        } catch (Exception ignored) {}
    }

    private void saveTransaction() {
        evaluateExpression(); // Final calculation
        String amountStr = currentInput.toString();
        
        if (amountStr.isEmpty() || selectedEnvelopeId == -1 || amountStr.equals("0")) {
            Toast.makeText(this, "Vui lòng nhập số tiền và chọn phong bì", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. SQLite tự động thêm dòng giao dịch mới
            ContentValues transValues = new ContentValues();
            transValues.put(DatabaseHelper.COLUMN_TRANS_TITLE, "Chi tiêu " + selectedEnvelopeName);
            transValues.put(DatabaseHelper.COLUMN_TRANS_AMOUNT, amount);
            transValues.put(DatabaseHelper.COLUMN_TRANS_ENV_ID, selectedEnvelopeId);
            db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, transValues);

            // 2. Lấy số tiền đó cộng dồn vào cột "Đã chi" và cập nhật "Số dư còn lại"
            Cursor cursor = db.query(DatabaseHelper.TABLE_ENVELOPES, 
                    new String[]{DatabaseHelper.COLUMN_ENV_SPENT, DatabaseHelper.COLUMN_ENV_REMAINING}, 
                    DatabaseHelper.COLUMN_ENV_ID + "=?", 
                    new String[]{String.valueOf(selectedEnvelopeId)}, null, null, null);
            
            if (cursor.moveToFirst()) {
                double spent = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_SPENT));
                double remaining = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_REMAINING));
                
                double newSpent = spent + amount;
                double newRemaining = remaining - amount;

                ContentValues envValues = new ContentValues();
                envValues.put(DatabaseHelper.COLUMN_ENV_SPENT, newSpent);
                envValues.put(DatabaseHelper.COLUMN_ENV_REMAINING, newRemaining);
                
                db.update(DatabaseHelper.TABLE_ENVELOPES, envValues, DatabaseHelper.COLUMN_ENV_ID + "=?", new String[]{String.valueOf(selectedEnvelopeId)});
            }
            cursor.close();

            db.setTransactionSuccessful();
            Toast.makeText(this, "Đã lưu giao dịch vào " + selectedEnvelopeName, Toast.LENGTH_LONG).show();
            
            // 3. Trở về Dashboard (Simulated by finishing or resetting)
            // finish(); // Uncomment to close activity and go back
            
            // Reset for next entry if staying on screen
            currentInput.setLength(0);
            updateAmountDisplay();
            loadEnvelopes(); // Refresh balances in UI
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }
}
