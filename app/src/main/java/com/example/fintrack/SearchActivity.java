package com.example.fintrack;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private LinearLayout llTodayList, llYesterdayList;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);
        llTodayList = findViewById(R.id.llTodayList);
        llYesterdayList = findViewById(R.id.llYesterdayList);
        etSearch = findViewById(R.id.etSearch);
        Button btnFilterSearch = findViewById(R.id.btnFilterSearch);

        btnFilterSearch.setOnClickListener(v -> performSearch());

        // Initial load (show all or some defaults)
        performSearch();
    }

    private void performSearch() {
        String keyword = etSearch.getText().toString().trim();
        loadRealTransactions(keyword);
    }

    private void loadRealTransactions(String keyword) {
        llTodayList.removeAllViews();
        llYesterdayList.removeAllViews();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Join transactions with envelopes to get category name and icon
        StringBuilder query = new StringBuilder();
        query.append("SELECT t.*, e.").append(DatabaseHelper.COLUMN_ENV_NAME)
             .append(", e.").append(DatabaseHelper.COLUMN_ENV_ICON)
             .append(" FROM ").append(DatabaseHelper.TABLE_TRANSACTIONS).append(" t")
             .append(" LEFT JOIN ").append(DatabaseHelper.TABLE_ENVELOPES).append(" e")
             .append(" ON t.").append(DatabaseHelper.COLUMN_TRANS_ENV_ID).append(" = e.").append(DatabaseHelper.COLUMN_ENV_ID)
             .append(" WHERE 1=1");

        List<String> args = new ArrayList<>();
        if (!keyword.isEmpty()) {
            query.append(" AND (t.").append(DatabaseHelper.COLUMN_TRANS_TITLE).append(" LIKE ? OR e.").append(DatabaseHelper.COLUMN_ENV_NAME).append(" LIKE ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }

        query.append(" ORDER BY t.").append(DatabaseHelper.COLUMN_TRANS_DATE).append(" DESC");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        boolean found = false;
        if (cursor.moveToFirst()) {
            found = true;
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_AMOUNT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_DATE));
                String catName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_NAME));
                String catIcon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_ICON));

                // For simplicity in this dummy UI, we add all to TodayList or YesterdayList based on some logic
                // In a real app, you'd group them by date
                if (date.contains("2024-07-16") || date.contains("Hôm nay")) {
                     addTransactionItem(llTodayList, catIcon, title, date, catName, -amount);
                } else {
                     addTransactionItem(llYesterdayList, catIcon, title, date, catName, -amount);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // If nothing found and keyword matches dummy items, show them for demo
        if (!found && (keyword.isEmpty() || "bún chả".contains(keyword.toLowerCase()))) {
            addTransactionItem(llTodayList, "🍔", "Bún chả dốc Hàng Than", "Hôm nay, 16/07", "Ăn uống", -65000);
        }
        if (!found && (keyword.isEmpty() || "xe buýt".contains(keyword.toLowerCase()))) {
            addTransactionItem(llTodayList, "🚌", "Vé xe buýt 09", "Hôm nay, 16/07", "Di chuyển", -7000);
        }
    }

    private void addTransactionItem(LinearLayout parent, String icon, String title, String time, String category, double amount) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_transaction, parent, false);
        
        TextView tvIcon = view.findViewById(R.id.tvTransIcon);
        TextView tvTitle = view.findViewById(R.id.tvTransTitle);
        TextView tvTime = view.findViewById(R.id.tvTransTime);
        TextView tvCategory = view.findViewById(R.id.tvTransCategory);
        TextView tvAmount = view.findViewById(R.id.tvTransAmount);

        tvIcon.setText(icon);
        tvTitle.setText(title != null ? title : "Chi tiêu " + category);
        tvTime.setText(time);
        tvCategory.setText(category);
        
        String amountStr = (amount > 0 ? "+" : "") + formatter.format(amount) + " đ";
        tvAmount.setText(amountStr);
        tvAmount.setTextColor(amount > 0 ? Color.parseColor("#27AE60") : Color.parseColor("#EB5757"));

        parent.addView(view);
    }
}
