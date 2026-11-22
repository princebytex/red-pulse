package com.redpulse.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.redpulse.app.R;
import com.redpulse.app.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 1200);
    }
}
