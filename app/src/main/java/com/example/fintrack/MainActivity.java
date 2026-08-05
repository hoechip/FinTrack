package com.example.fintrack;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.fintrack.data.FirebaseHelper;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout layoutMenuChinh;
    FrameLayout fragmentContainer;
    MaterialCardView cardManHinh5, cardManHinh6;

    FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseHelper = FirebaseHelper.layThucThe();
        firebaseHelper.langNgheKeHoachNganSach(null);

        layoutMenuChinh = findViewById(R.id.layoutMenuChinh);
        fragmentContainer = findViewById(R.id.fragment_container);
        cardManHinh5 = findViewById(R.id.cardManHinh5);
        cardManHinh6 = findViewById(R.id.cardManHinh6);

        cardManHinh5.setOnClickListener(this);
        cardManHinh6.setOnClickListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                hienThiMenuChinh();
            } else {
                anMenuChinh();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cardManHinh5) {
            moFragment(new PhanBoNganSachFragment());
        } else if (v.getId() == R.id.cardManHinh6) {
            moFragment(new TienDoNganSachFragment());
        }
    }

    public void moFragment(Fragment fragment) {
        anMenuChinh();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void showHomeScreen() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            hienThiMenuChinh();
        }
    }

    private void hienThiMenuChinh() {
        if (layoutMenuChinh != null && fragmentContainer != null) {
            layoutMenuChinh.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
        }
    }

    private void anMenuChinh() {
        if (layoutMenuChinh != null && fragmentContainer != null) {
            layoutMenuChinh.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }
}