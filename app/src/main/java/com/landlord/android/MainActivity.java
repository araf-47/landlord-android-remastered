package com.landlord.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.landlord.android.core.auth.BiometricLockPrefs;
import com.landlord.android.core.auth.SessionManager;
import com.landlord.android.feature.auth.AuthActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        if (BiometricLockPrefs.isEnabled(this) && biometricAvailable()) {
            showLockScreen();
        } else {
            showMainContent();
        }
    }

    private boolean biometricAvailable() {
        BiometricManager manager = BiometricManager.from(this);
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showLockScreen() {
        setContentView(R.layout.activity_lock);
        findViewById(R.id.unlock_button).setOnClickListener(v -> promptBiometric());
        promptBiometric();
    }

    private void promptBiometric() {
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        showMainContent();
                    }
                });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock LandLord")
                .setSubtitle("Authenticate using your biometric credentials")
                .setNegativeButtonText("Cancel")
                .build();

        prompt.authenticate(info);
    }

    private void showMainContent() {
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main_root);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottomInset;
            if (ime.bottom > 0) {
                // Keyboard is open - float content directly above the keyboard
                bottomInset = ime.bottom;
            } else if (bottomNav != null && bottomNav.getVisibility() == View.VISIBLE) {
                // Bottom nav is visible - bottom nav handles system nav bar spacing
                bottomInset = 0;
            } else {
                // Inner screen with hidden bottom nav - respect system navigation bar
                bottomInset = systemBars.bottom;
            }

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Dynamically show bottom nav on top-level destinations, hide on inner screens (e.g. Chat, AI, Detail)
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.nav_dashboard || id == R.id.nav_properties ||
                    id == R.id.nav_tenants || id == R.id.nav_payments || id == R.id.nav_more) {
                    if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
                } else {
                    if (bottomNav != null) bottomNav.setVisibility(View.GONE);
                }
                ViewCompat.requestApplyInsets(root);
            });
        }
    }
}
