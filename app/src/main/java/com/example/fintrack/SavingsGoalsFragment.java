package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SavingsGoalsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment này đóng vai trò là "cầu nối" để mở Activity cũ của bạn
        // Tuy nhiên, để trải nghiệm mượt mà, ta nên chuyển Activity thành Fragment sau này.
        // Hiện tại, ta sẽ mở Activity ngay khi Fragment được gắn vào.
        return inflater.inflate(R.layout.fragment_savings_goals_bridge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_open_savings_goals).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), M9DanhSachMucTieuActivity.class);
            startActivity(intent);
        });
        
        // Tự động mở luôn khi vào fragment này
        Intent intent = new Intent(getActivity(), M9DanhSachMucTieuActivity.class);
        startActivity(intent);
    }
}
