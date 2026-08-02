package com.example.fintrack;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fintrack.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class M3DashboardFragment extends Fragment {

    private static final String TAG = "M3DashboardFragment";
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Cấu hình RecyclerView với dữ liệu mẫu
        if (binding.rvM3RecentTransactions != null) {
            binding.rvM3RecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvM3RecentTransactions.setHasFixedSize(true);

            List<Transaction> mockData = new ArrayList<>();
            mockData.add(new Transaction("Mèo Mun Cafe", "Hôm nay • 12:30", "-55k", android.R.drawable.ic_menu_gallery));
            mockData.add(new Transaction("Mua trà sữa", "Hôm nay • 15:00", "-30k", android.R.drawable.ic_menu_gallery));
            mockData.add(new Transaction("GrabFood", "Hôm qua • 19:20", "-120k", android.R.drawable.ic_menu_gallery));
            mockData.add(new Transaction("Nạp tiền điện thoại", "20/10 • 09:00", "-50k", android.R.drawable.ic_menu_gallery));

            RecentTransactionAdapter adapter = new RecentTransactionAdapter(mockData);
            binding.rvM3RecentTransactions.setAdapter(adapter);
        }

        // 2. Xử lý click Xem tất cả
        binding.btnM3ViewAllProgress.setOnClickListener(v -> {
            Log.d(TAG, "btnM3ViewAllProgress clicked");
            NavHostFragment.findNavController(M3DashboardFragment.this)
                    .navigate(R.id.action_m3_to_m4);
        });

        // 3. Mở Menu trượt (Drawer) khi nhấn dấu 3 gạch
        binding.btnM3Menu.setOnClickListener(v -> {
            Log.d(TAG, "btnM3Menu clicked");
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            } else {
                Toast.makeText(getContext(), "Drawer not available", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Xử lý click Xem lịch sử
        binding.btnM3ViewHistory.setOnClickListener(v -> {
            Log.d(TAG, "btnM3ViewHistory clicked");
            NavHostFragment.findNavController(M3DashboardFragment.this)
                    .navigate(R.id.fragment_history);
        });
        
        // 5. Click Avatar
        binding.imgM3Avatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Profile clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
