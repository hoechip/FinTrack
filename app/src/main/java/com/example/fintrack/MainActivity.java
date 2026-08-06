package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainActivity");

        try {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setSupportActionBar(binding.toolbarMainTop);

            // 1. Khởi tạo Firebase Helper từ nhánh của Quỳnh
            try {
                firebaseHelper = FirebaseHelper.layThucThe();
                if (firebaseHelper != null) {
                    firebaseHelper.langNgheKeHoachNganSach(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khởi tạo FirebaseHelper: ", e);
            }

            // 2. Cấu hình Navigation Component
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);

            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();

                appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.m3_dashboard, R.id.m4_envelopes, R.id.fragment_chart, R.id.fragment_history, R.id.fragment_settings)
                        .setOpenableLayout(binding.drawerLayout)
                        .build();

                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                NavigationUI.setupWithNavController(binding.bottomNav, navController);
                NavigationUI.setupWithNavController(binding.navViewDrawer, navController);

                // Mở ProfileActivity từ Header của Navigation Drawer
                View headerView = binding.navViewDrawer.getHeaderView(0);
                if (headerView != null) {
                    View imgAvatar = headerView.findViewById(R.id.img_nav_header_avatar);
                    if (imgAvatar != null) {
                        imgAvatar.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                        });
                    }
                }

                // Ẩn / Hiện BottomNav & FAB theo Màn hình
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    Log.d(TAG, "onDestinationChanged: " + destination.getLabel());
                    if (destination.getId() == R.id.m4_envelopes) {
                        binding.bottomNav.setVisibility(View.GONE);
                        binding.fabM3Add.setVisibility(View.GONE);
                    } else {
                        binding.bottomNav.setVisibility(View.VISIBLE);
                        binding.fabM3Add.setVisibility(View.VISIBLE);
                    }
                });
            }

            // 4. Nút FAB mở màn hình Thêm giao dịch
            binding.fabM3Add.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvatarToDrawer();
    }

    private void loadAvatarToDrawer() {
        if (binding == null) return;
        
        View headerView = binding.navViewDrawer.getHeaderView(0);
        if (headerView == null) return;
        
        android.widget.ImageView imgAvatar = headerView.findViewById(R.id.img_nav_header_avatar);
        android.widget.TextView txtName = headerView.findViewById(R.id.txt_nav_header_name);
        android.widget.TextView txtEmail = headerView.findViewById(R.id.txt_nav_header_email);
        
        if (imgAvatar == null) return;

        android.content.SharedPreferences sp = getSharedPreferences("FinTrack", MODE_PRIVATE);
        String avatarPath = sp.getString("AVATAR_PATH", "");
        String fullName = sp.getString("FULLNAME", "");
        String email = sp.getString("CURRENT_EMAIL", sp.getString("EMAIL", ""));

        if (txtName != null && !fullName.isEmpty()) txtName.setText(fullName);
        if (txtEmail != null && !email.isEmpty()) txtEmail.setText(email);

        if (!avatarPath.isEmpty()) {
            java.io.File file = new java.io.File(avatarPath);
            if (file.exists()) {
                imgAvatar.setImageURI(null);
                imgAvatar.setImageURI(android.net.Uri.fromFile(file));
            } else {
                imgAvatar.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            imgAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    // Xử lý chuyển Fragment Ngân sách từ code của Quỳnh
    @Override
    public void onClick(View v) {
        // Xử lý sự kiện click nếu cần
    }


    public void openDrawer() {
        if (binding != null && binding.drawerLayout != null) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}