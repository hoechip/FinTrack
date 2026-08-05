package com.example.fintrack;

import android.app.Activity;
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
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(getContext(), "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                    }
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
                
                // Thực hiện sao lưu thực tế ở đây (ví dụ: backup database SQLite hoặc JSON)
                performBackup(account);
            }
        });

        binding.txtConnectDrive.setOnClickListener(v -> {
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
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
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
        GoogleSignInAccount account = driveHelper.getSignedInAccount();
        if (account != null) {
            binding.txtConnectDrive.setText("Đã kết nối: " + account.getEmail());
        } else {
            binding.txtConnectDrive.setText("Chưa kết nối Google Drive");
            binding.switchAutoBackup.setChecked(false);
        }
    }

    private void performBackup(GoogleSignInAccount account) {
        Toast.makeText(getContext(), "Đang chuẩn bị dữ liệu sao lưu...", Toast.LENGTH_SHORT).show();
        
        // TODO: Lấy file dữ liệu thực tế (VD: database app) để upload
        // Ở đây chỉ minh họa bằng việc tạo một file tạm
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
