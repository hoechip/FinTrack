package com.example.fintrack;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fintrack.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutMainRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        setSupportActionBar(binding.toolbarMainTop);

        // 1. Tìm NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 2. Cấu hình AppBarConfiguration cho Drawer và BottomNav
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.m3_dashboard, R.id.fragment_chart, R.id.m4_envelopes,
                    R.id.fragment_monthly_budget, R.id.fragment_savings_goals,
                    R.id.fragment_history, R.id.fragment_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // 3. Kết nối Bottom Navigation
            if (binding.navMainBottom != null) {
                NavigationUI.setupWithNavController(binding.navMainBottom, navController);
            }

            // 4. Kết nối Navigation Drawer
            if (binding.navViewDrawer != null) {
                NavigationUI.setupWithNavController(binding.navViewDrawer, navController);
            }

            // 5. Ẩn/Hiện Bottom Navigation và FAB khi chuyển đổi màn hình
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.m4_envelopes) {
                    binding.navMainBottom.setVisibility(View.GONE);
                    binding.fabM3Add.setVisibility(View.GONE);
                } else {
                    binding.navMainBottom.setVisibility(View.VISIBLE);
                    binding.fabM3Add.setVisibility(View.VISIBLE);
                }
            });
        }

        // 6. Nút FAB (+)
        if (binding.fabM3Add != null) {
            binding.fabM3Add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, getString(R.string.msg_in_development), Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.fabM3Add)
                            .setAction("Action", null).show();
                }
            });
        }
    }

    /**
     * Hàm hỗ trợ mở Menu trượt từ Fragment
     */
    public void openDrawer() {
        if (binding.drawerLayout != null) {
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

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout != null && binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}