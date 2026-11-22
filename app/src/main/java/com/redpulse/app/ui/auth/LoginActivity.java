package com.redpulse.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.redpulse.app.R;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.ui.main.MainActivity;
import com.redpulse.app.utils.FirestoreCallback;
import com.redpulse.app.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private ProgressBar progressBar;
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgot = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Enter a valid email");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("Minimum 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                task.getException() != null ? task.getException().getMessage() : "Login failed",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser == null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!firebaseUser.isEmailVerified()) {
                        firebaseUser.sendEmailVerification();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Please verify your email first. We sent the verification link again.",
                                Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        return;
                    }

                    continueWithProfile(firebaseUser);
                });
    }

    private void continueWithProfile(FirebaseUser firebaseUser) {
        userRepository.fetchUser(firebaseUser.getUid(), new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                if (user.isDeleted() || user.isBlocked()) {
                    String reason = user.getDeletedReason() == null || user.getDeletedReason().trim().isEmpty()
                            ? "This account has been blocked by admin."
                            : user.getDeletedReason();
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(LoginActivity.this, reason, Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid())
                        .update("verified", true, "updatedAt", Timestamp.now());

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(LoginActivity.this,
                        message == null || message.trim().isEmpty()
                                ? "Your profile could not be loaded. Please contact support."
                                : message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
