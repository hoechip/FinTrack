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

import com.example.fintrack.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainActivity");

        try {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            Log.d(TAG, "onCreate: Layout inflated and set");

            setSupportActionBar(binding.toolbarMainTop);

            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);

            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                
                // Setup AppBarConfiguration
                appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.m3_dashboard, R.id.m4_envelopes, R.id.fragment_chart, R.id.fragment_history, R.id.fragment_settings)
                        .setOpenableLayout(binding.drawerLayout)
                        .build();

                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                NavigationUI.setupWithNavController(binding.bottomNav, navController);
                NavigationUI.setupWithNavController(binding.navViewDrawer, navController);

                // Setup Profile click in Drawer Header
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

            binding.fabM3Add.setOnClickListener(v ->
                    Snackbar.make(v, getString(R.string.msg_in_development), Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.fabM3Add)
                            .setAction("Action", null).show()
            );

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
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