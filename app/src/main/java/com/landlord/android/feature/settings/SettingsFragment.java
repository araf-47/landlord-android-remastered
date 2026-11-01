package com.landlord.android.feature.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.landlord.android.R;
import com.landlord.android.core.auth.BiometricLockPrefs;
import com.landlord.android.core.auth.SessionManager;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.DatabaseSeeder;
import com.landlord.android.feature.auth.AuthActivity;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = SessionManager.getInstance(requireContext());

        EditText inputName = view.findViewById(R.id.input_name);
        EditText inputEmail = view.findViewById(R.id.input_email);
        EditText inputPhone = view.findViewById(R.id.input_phone);
        MaterialButton btnSaveProfile = view.findViewById(R.id.save_profile_button);

        MaterialSwitch switchBiometric = view.findViewById(R.id.switch_biometric_lock);
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        MaterialButton btnResetDemo = view.findViewById(R.id.btn_reset_demo_data);
        MaterialButton btnLogout = view.findViewById(R.id.logout_button);

        inputName.setText(session.getDisplayName());
        inputEmail.setText(session.getEmail());
        inputPhone.setText(session.getPhone());

        btnSaveProfile.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();

            if (!name.isEmpty()) {
                session.updateProfile(name, email, phone);
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            }
        });

        // Biometric toggle
        switchBiometric.setChecked(BiometricLockPrefs.isEnabled(requireContext()));
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BiometricLockPrefs.setEnabled(requireContext(), isChecked);
            Toast.makeText(requireContext(), isChecked ? "Biometric lock enabled" : "Biometric lock disabled", Toast.LENGTH_SHORT).show();
        });

        // Dark mode toggle
        switchDarkMode.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Reset demo data
        btnResetDemo.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Reset to Demo Showcase Data?")
                    .setMessage("This will reset all properties, units, tenants, invoices, and payments back to the initial pristine demo state. Continue?")
                    .setPositiveButton("Reset Now", (dialog, which) -> {
                        AppExecutors.DB.execute(() -> {
                            AppDatabase db = AppDatabase.getInstance(requireContext());
                            DatabaseSeeder.seed(db);
                            AppExecutors.runOnMain(() ->
                                    Toast.makeText(requireContext(), "Database successfully restored to demo state!", Toast.LENGTH_LONG).show()
                            );
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        session.logout();
                        Intent intent = new Intent(requireContext(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
