package com.redpulse.app.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.utils.ValidationUtils;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        EditText etEmail = findViewById(R.id.etEmail);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        Button btnReset = findViewById(R.id.btnResetPassword);

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!ValidationUtils.isValidEmail(email)) {
                etEmail.setError("Enter a valid email");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
