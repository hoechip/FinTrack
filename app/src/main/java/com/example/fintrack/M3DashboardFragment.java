package com.example.fintrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fintrack.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class M3DashboardFragment extends Fragment {

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
        if (binding.btnM3ViewAllProgress != null) {
            binding.btnM3ViewAllProgress.setOnClickListener(v ->
                    NavHostFragment.findNavController(M3DashboardFragment.this)
                            .navigate(R.id.action_m3_to_m4)
            );
        }

        // 3. Mở Menu trượt (Drawer) khi nhấn dấu 3 gạch
        if (binding.btnM3Menu != null) {
            binding.btnM3Menu.setOnClickListener(v -> {
                // TODO: Implement drawer or menu action
                // if (getActivity() instanceof MainActivity) {
                //     ((MainActivity) getActivity()).openDrawer();
                // }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
