package com.example.fintrack;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private LinearLayout llTodayList, llYesterdayList;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        llTodayList = view.findViewById(R.id.llTodayList);
        llYesterdayList = view.findViewById(R.id.llYesterdayList);
        etSearch = view.findViewById(R.id.etSearch);
        Button btnFilterSearch = view.findViewById(R.id.btnFilterSearch);

        btnFilterSearch.setOnClickListener(v -> performSearch());

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

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_AMOUNT));
                String rawDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_DATE));
                String catIcon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_ICON));

                String displayDate = formatDisplayDate(rawDate);

                if (displayDate.startsWith("Hôm nay")) {
                     addTransactionItem(llTodayList, catIcon, title, displayDate, "", -amount);
                } else {
                     addTransactionItem(llYesterdayList, catIcon, title, displayDate, "", -amount);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private String formatDisplayDate(String rawDate) {
        try {
            SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dbSdf.parse(rawDate);
            if (date == null) return rawDate;

            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.setTime(date);

            SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeStr = timeSdf.format(date);

            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "Hôm nay, " + new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date);
            }

            now.add(Calendar.DAY_OF_YEAR, -1);
            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "Hôm qua, " + new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date);
            }

            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return rawDate;
        }
    }

    private void addTransactionItem(LinearLayout parent, String iconTag, String title, String time, String category, double amount) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_transaction, parent, false);
        
        android.widget.ImageView imgIcon = view.findViewById(R.id.imgTransIcon);
        TextView tvIconEmoji = view.findViewById(R.id.tvTransIcon);
        TextView tvTitle = view.findViewById(R.id.tvTransTitle);
        TextView tvTime = view.findViewById(R.id.tvTransTime);
        TextView tvCategory = view.findViewById(R.id.tvTransCategory);
        TextView tvAmount = view.findViewById(R.id.tvTransAmount);

        // Logic hiển thị icon màu sắc đồng bộ
        if (iconTag != null && !iconTag.isEmpty()) {
            int resId = getResources().getIdentifier("ic_" + iconTag, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                imgIcon.setVisibility(View.VISIBLE);
                imgIcon.setImageResource(resId);
                tvIconEmoji.setVisibility(View.GONE);
            } else {
                imgIcon.setVisibility(View.GONE);
                tvIconEmoji.setVisibility(View.VISIBLE);
                tvIconEmoji.setText(iconTag);
            }
        } else {
            imgIcon.setVisibility(View.VISIBLE);
            imgIcon.setImageResource(R.drawable.ic_wallet);
            tvIconEmoji.setVisibility(View.GONE);
        }

        tvTitle.setText(title);
        tvTime.setText(time);
        tvCategory.setText(category);
        if (category.isEmpty()) tvCategory.setVisibility(View.GONE);
        
        String amountStr = (amount > 0 ? "+" : "") + formatter.format(amount) + " đ";
        tvAmount.setText(amountStr);
        tvAmount.setTextColor(amount > 0 ? Color.parseColor("#27AE60") : Color.parseColor("#EB5757"));

        parent.addView(view);
    }
}
