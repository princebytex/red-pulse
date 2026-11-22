package com.redpulse.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.redpulse.app.R;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.ui.admin.AdminDashboardFragment;
import com.redpulse.app.ui.admin.AdminNoticesFragment;
import com.redpulse.app.ui.admin.AdminRequestsFragment;
import com.redpulse.app.ui.admin.AdminUsersFragment;
import com.redpulse.app.ui.auth.LoginActivity;
import com.redpulse.app.ui.credits.CreditsFragment;
import com.redpulse.app.ui.dashboard.HomeFragment;
import com.redpulse.app.ui.donor.FindDonorFragment;
import com.redpulse.app.ui.history.HistoryFragment;
import com.redpulse.app.ui.leaderboard.LeaderboardFragment;
import com.redpulse.app.ui.notifications.NotificationsFragment;
import com.redpulse.app.ui.profile.ProfileFragment;
import com.redpulse.app.ui.request.CreateRequestFragment;
import com.redpulse.app.ui.request.RequestFeedFragment;
import com.redpulse.app.ui.request.RequestsFragment;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private final UserRepository userRepository = new UserRepository();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        bindDrawerHeaderPlaceholder();
        attachBackStackListener();

        notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        });
        requestNotificationPermission();
        loadCurrentUser(savedInstanceState == null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshCurrentScreen();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCurrentScreen() {
        loadCurrentUser(false);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment == null) return;
        try {
            Fragment refreshed = currentFragment.getClass().getDeclaredConstructor().newInstance();
            if (currentFragment.getArguments() != null) {
                refreshed.setArguments(new Bundle(currentFragment.getArguments()));
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, refreshed)
                    .commit();
            toolbar.setTitle(resolveTitle(refreshed));
            updateCheckedMenu(refreshed);
            Toast.makeText(this, "Panel refreshed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not refresh this screen right now.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentUser(boolean openDefaultScreen) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            logoutSilently();
            return;
        }

        userRepository.fetchUser(firebaseUser.getUid(), new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User data) {
                currentUser = data;
                if (data.isDeleted() || data.isBlocked()) {
                    Toast.makeText(MainActivity.this,
                            data.getDeletedReason() == null || data.getDeletedReason().trim().isEmpty()
                                    ? "Your account has been blocked by admin."
                                    : data.getDeletedReason(),
                            Toast.LENGTH_LONG).show();
                    logoutSilently();
                    return;
                }
                applyRoleUi(data);
                if (openDefaultScreen) {
                    if (isAdmin()) {
                        openFragment(new AdminDashboardFragment(), false);
                    } else {
                        openFragment(new HomeFragment(), false);
                    }
                } else {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (currentFragment != null) {
                        toolbar.setTitle(resolveTitle(currentFragment));
                        updateCheckedMenu(currentFragment);
                    }
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this,
                        message == null || message.trim().isEmpty() ? "Could not load your profile." : message,
                        Toast.LENGTH_LONG).show();
                logoutSilently();
            }
        });
    }

    private void bindDrawerHeaderPlaceholder() {
        TextView tvName = navigationView.getHeaderView(0).findViewById(R.id.tvDrawerUserName);
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvDrawerUserEmail);
        TextView tvRole = navigationView.getHeaderView(0).findViewById(R.id.tvDrawerRole);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tvName.setText(getString(R.string.app_name));
        tvEmail.setText(firebaseUser != null && firebaseUser.getEmail() != null
                ? firebaseUser.getEmail()
                : getString(R.string.drawer_tagline));
        tvRole.setText("Loading profile...");
    }

    private void applyRoleUi(@NonNull User user) {
        boolean admin = Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole());
        navigationView.getMenu().setGroupVisible(R.id.group_user_root, !admin);
        navigationView.getMenu().setGroupVisible(R.id.group_admin_root, admin);
        navigationView.getMenu().setGroupVisible(R.id.group_common_root, true);

        TextView tvName = navigationView.getHeaderView(0).findViewById(R.id.tvDrawerUserName);
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvDrawerUserEmail);
        TextView tvRole = navigationView.getHeaderView(0).findViewById(R.id.tvDrawerRole);
        tvName.setText(user.getFullName() == null || user.getFullName().trim().isEmpty() ? getString(R.string.app_name) : user.getFullName());
        tvEmail.setText(user.getEmail() == null || user.getEmail().trim().isEmpty() ? getString(R.string.drawer_tagline) : user.getEmail());
        tvRole.setText(admin ? "Admin CMS access" : "Donor community access");
        toolbar.setSubtitle(admin ? "Admin" : "Member");
    }

    private void attachBackStackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment != null) {
                toolbar.setTitle(resolveTitle(currentFragment));
                updateCheckedMenu(currentFragment);
            }
        });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_home) openFragment(new HomeFragment(), false);
        else if (id == R.id.menu_request_blood) openFragment(new CreateRequestFragment(), true);
        else if (id == R.id.menu_live_requests) openFragment(new RequestFeedFragment(), true);
        else if (id == R.id.menu_my_requests) openFragment(new RequestsFragment(), true);
        else if (id == R.id.menu_find_donor) openFragment(new FindDonorFragment(), true);
        else if (id == R.id.menu_my_credits) openFragment(new CreditsFragment(), true);
        else if (id == R.id.menu_history) openFragment(new HistoryFragment(), true);
        else if (id == R.id.menu_leaderboard) openFragment(new LeaderboardFragment(), true);
        else if (id == R.id.menu_admin_dashboard) openFragment(new AdminDashboardFragment(), false);
        else if (id == R.id.menu_admin_requests) openFragment(new AdminRequestsFragment(), true);
        else if (id == R.id.menu_admin_users) openFragment(new AdminUsersFragment(), true);
        else if (id == R.id.menu_admin_notices) openFragment(new AdminNoticesFragment(), true);
        else if (id == R.id.menu_profile) openFragment(new ProfileFragment(), true);
        else if (id == R.id.menu_notifications) openFragment(new NotificationsFragment(), true);
        else if (id == R.id.menu_logout) logout();
        drawerLayout.closeDrawers();
        return true;
    }

    public void openFragment(@NonNull Fragment fragment, boolean addToBackStack) {
        if (addToBackStack) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        } else {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
        toolbar.setTitle(resolveTitle(fragment));
        updateCheckedMenu(fragment);
    }

    private String resolveTitle(Fragment fragment) {
        if (fragment instanceof HomeFragment) return getString(R.string.dashboard_title);
        if (fragment instanceof CreateRequestFragment) return getString(R.string.request_blood_title);
        if (fragment instanceof RequestFeedFragment) return getString(R.string.live_requests_title);
        if (fragment instanceof RequestsFragment) return getString(R.string.my_requests_title);
        if (fragment instanceof FindDonorFragment) return getString(R.string.find_donor_title);
        if (fragment instanceof CreditsFragment) return getString(R.string.my_credits_title);
        if (fragment instanceof HistoryFragment) return getString(R.string.history_title);
        if (fragment instanceof LeaderboardFragment) return getString(R.string.leaderboard_title);
        if (fragment instanceof AdminDashboardFragment) return getString(R.string.admin_dashboard_title);
        if (fragment instanceof AdminRequestsFragment) return getString(R.string.admin_review_requests_title);
        if (fragment instanceof AdminUsersFragment) return getString(R.string.admin_users_title);
        if (fragment instanceof AdminNoticesFragment) return getString(R.string.admin_notices_title);
        if (fragment instanceof ProfileFragment) return getString(R.string.profile_title);
        if (fragment instanceof NotificationsFragment) return getString(R.string.notifications_title);
        return getString(R.string.app_name);
    }

    private void updateCheckedMenu(Fragment fragment) {
        int itemId = isAdmin() ? R.id.menu_admin_dashboard : R.id.menu_home;
        if (fragment instanceof CreateRequestFragment) itemId = R.id.menu_request_blood;
        else if (fragment instanceof RequestFeedFragment) itemId = R.id.menu_live_requests;
        else if (fragment instanceof RequestsFragment) itemId = R.id.menu_my_requests;
        else if (fragment instanceof FindDonorFragment) itemId = R.id.menu_find_donor;
        else if (fragment instanceof CreditsFragment) itemId = R.id.menu_my_credits;
        else if (fragment instanceof HistoryFragment) itemId = R.id.menu_history;
        else if (fragment instanceof LeaderboardFragment) itemId = R.id.menu_leaderboard;
        else if (fragment instanceof AdminDashboardFragment) itemId = R.id.menu_admin_dashboard;
        else if (fragment instanceof AdminRequestsFragment) itemId = R.id.menu_admin_requests;
        else if (fragment instanceof AdminUsersFragment) itemId = R.id.menu_admin_users;
        else if (fragment instanceof AdminNoticesFragment) itemId = R.id.menu_admin_notices;
        else if (fragment instanceof ProfileFragment) itemId = R.id.menu_profile;
        else if (fragment instanceof NotificationsFragment) itemId = R.id.menu_notifications;
        navigationView.setCheckedItem(itemId);
    }

    private boolean isAdmin() {
        return currentUser != null && Constants.ROLE_ADMIN.equalsIgnoreCase(currentUser.getRole());
    }

    private void logoutSilently() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        logoutSilently();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
            return;
        }
        super.onBackPressed();
    }
}
