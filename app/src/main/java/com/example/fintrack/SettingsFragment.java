package com.example.fintrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fintrack.databinding.FragmentSettingsBinding;
import com.example.fintrack.util.GoogleDriveHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private GoogleDriveHelper driveHelper;
    private ActivityResultLauncher<Intent> driveSignInLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driveHelper = new GoogleDriveHelper(requireContext());

        // Đăng ký Launcher để xử lý kết quả đăng nhập Google
        driveSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
        );

        updateDriveUI();

        binding.switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                GoogleSignInAccount account = driveHelper.getSignedInAccount();
                if (account == null) {
                    Toast.makeText(getContext(), "Vui lòng kết nối Google Drive trước", Toast.LENGTH_SHORT).show();
                    binding.switchAutoBackup.setChecked(false);
                    return;
                }
                
                performBackup(account);
            }
        });

        binding.btnConnectDrive.setOnClickListener(v -> {
            com.google.android.gms.common.GoogleApiAvailability availability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
            int resultCode = availability.isGooglePlayServicesAvailable(requireContext());
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                if (availability.isUserResolvableError(resultCode)) {
                    availability.getErrorDialog(requireActivity(), resultCode, 9000).show();
                } else {
                    Toast.makeText(getContext(), "Thiết bị không hỗ trợ Google Play Services", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            GoogleSignInAccount account = driveHelper.getSignedInAccount();
            if (account == null) {
                driveHelper.requestSignIn(driveSignInLauncher);
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Thông tin kết nối")
                        .setMessage("Bạn đã kết nối với tài khoản: " + account.getEmail())
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            driveHelper.signOut(() -> {
                                Toast.makeText(getContext(), "Đã ngắt kết nối Google Drive", Toast.LENGTH_SHORT).show();
                                updateDriveUI();
                            });
                        })
                        .setNegativeButton("Đóng", null)
                        .show();
            }
        });

        binding.btnSettingsLogout.setOnClickListener(v -> {
            // 1. Xóa dữ liệu cục bộ tài khoản hiện tại
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            dbHelper.clearAllData();
            
            SharedPreferences sp = requireContext().getSharedPreferences("FinTrack", Context.MODE_PRIVATE);
            sp.edit().clear().apply();

            // 2. Ngắt kết nối Google Drive hoàn toàn và sau đó Đăng xuất Firebase
            driveHelper.signOut(() -> {
                FirebaseAuth.getInstance().signOut();
                
                if (isAdded()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            completedTask.getResult(ApiException.class);
            Toast.makeText(getContext(), "Kết nối Google Drive thành công!", Toast.LENGTH_SHORT).show();
            updateDriveUI();
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Lỗi kết nối: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDriveUI() {
        if (binding == null) return;
        GoogleSignInAccount account = driveHelper.getSignedInAccount();
        if (account != null) {
            binding.txtDriveStatus.setText("Đã kết nối: " + account.getEmail());
            binding.btnConnectDrive.setText("Quản lý kết nối");
            binding.btnConnectDrive.setIconResource(R.drawable.ic_settings);
            binding.btnConnectDrive.setIconTintResource(R.color.text_secondary);
            binding.btnConnectDrive.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary));
        } else {
            binding.txtDriveStatus.setText("Chưa kết nối Google Drive");
            binding.btnConnectDrive.setText("Kết nối Google Drive");
            binding.btnConnectDrive.setIconResource(R.drawable.ic_link);
            binding.btnConnectDrive.setIconTintResource(R.color.emerald_green);
            binding.btnConnectDrive.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.emerald_green));
            binding.switchAutoBackup.setChecked(false);
        }
    }

    private void performBackup(GoogleSignInAccount account) {
        Toast.makeText(getContext(), "Đang chuẩn bị dữ liệu sao lưu...", Toast.LENGTH_SHORT).show();
        
        java.io.File cacheDir = requireContext().getCacheDir();
        java.io.File tempFile = new java.io.File(cacheDir, "fintrack_backup.json");
        
        try {
            java.io.FileWriter writer = new java.io.FileWriter(tempFile);
            writer.write("{\"backup_date\": \"" + System.currentTimeMillis() + "\", \"data\": \"sample\"}");
            writer.close();

            driveHelper.uploadFileToDrive(account, tempFile, "application/json", new GoogleDriveHelper.DriveCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Sao lưu thành công")
                                    .setMessage("Dữ liệu đã được lưu an toàn trên Google Drive (App Data).\nID: " + result)
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi sao lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi tạo file sao lưu", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
