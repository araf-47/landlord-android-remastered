package com.landlord.android.feature.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.landlord.android.MainActivity;
import com.landlord.android.R;
import com.landlord.android.core.auth.SessionManager;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already logged in, navigate straight to MainActivity
        if (SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        MaterialButton quickDemoBtn = findViewById(R.id.btn_quick_demo_login);
        TextInputEditText usernameInput = findViewById(R.id.username_input);
        TextInputEditText passwordInput = findViewById(R.id.password_input);
        MaterialButton loginButton = findViewById(R.id.login_button);
        ProgressBar progress = findViewById(R.id.login_progress);
        TextView errorText = findViewById(R.id.error_text);

        quickDemoBtn.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            quickDemoBtn.setEnabled(false);
            SessionManager.getInstance(this).loginDemoLandlord();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        loginButton.setOnClickListener(v -> {
            String username = String.valueOf(usernameInput.getText()).trim();
            String password = String.valueOf(passwordInput.getText()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorText.setText("Please enter username and password");
                errorText.setVisibility(View.VISIBLE);
                return;
            }

            errorText.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            SessionManager.getInstance(this).loginCustom(username, password);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
