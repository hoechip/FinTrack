package com.example.fintrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.fintrack.databinding.FragmentStatisticsBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private FirebaseFirestore db;
    private EnvelopeAdapter adapter;
    private List<Envelope> envelopeList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        fetchStatistics();
        setupDatePicker();
        
        // Load fake data for demo
        loadFakeEnvelopes();
    }

    private void setupRecyclerView() {
        envelopeList = new ArrayList<>();
        adapter = new EnvelopeAdapter(envelopeList);
        binding.rvM5Envelopes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvM5Envelopes.setAdapter(adapter);
    }

    private void loadFakeEnvelopes() {
        envelopeList.clear();
        envelopeList.add(new Envelope("Ăn uống", 4500000, 7000000, "ic_food", "#EB5757"));
        envelopeList.add(new Envelope("Di chuyển", 2800000, 6000000, "ic_transport", "#BDB76B"));
        envelopeList.add(new Envelope("Thuê nhà", 2500000, 12500000, "ic_home", "#27AE60"));
        adapter.notifyDataSetChanged();
    }

    private void setupDatePicker() {
        binding.btnM5DatePicker.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            
            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Xử lý ngày được chọn
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private void fetchStatistics() {
        db.collection("transactions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        double incomeSum = 0;
                        double expenseSum = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String type = document.getString("type");
                            Double amount = document.getDouble("amount");
                            if (amount != null && type != null) {
                                switch (type) {
                                    case "income":
                                        incomeSum += amount;
                                        break;
                                    case "expense":
                                        expenseSum += amount;
                                        break;
                                }
                            }
                        }
                        updateUI(incomeSum, expenseSum);
                    }
                });
    }

    private void updateUI(double income, double expense) {
        if (binding == null) return;

        double balance = income - expense;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.txtM5Balance.setText(currencyFormat.format(balance));

        double total = income + expense;
        if (total > 0) {
            int incomePercent = (int) ((income / total) * 100);
            int expensePercent = (int) ((expense / total) * 100);

            binding.pbM5Income.setProgress(incomePercent);
            binding.pbM5Expense.setProgress(expensePercent);
            
            binding.pbM5Expense.setRotation(-90);
            binding.pbM5Income.setRotation((float) (expensePercent * 3.6 - 90));
        } else {
            binding.pbM5Income.setProgress(0);
            binding.pbM5Expense.setProgress(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
