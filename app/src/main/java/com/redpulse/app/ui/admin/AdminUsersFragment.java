package com.redpulse.app.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.CreditTransaction;
import com.redpulse.app.data.model.Donation;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.AdminRepository;
import com.redpulse.app.ui.adapter.AdminUserAdapter;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUsersFragment extends Fragment {
    private final AdminRepository adminRepository = new AdminRepository();
    private final List<User> allUsers = new ArrayList<>();
    private AdminUserAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText etSearch = view.findViewById(R.id.etAdminUserSearch);
        tvEmpty = view.findViewById(R.id.tvEmptyState);
        RecyclerView recyclerView = view.findViewById(R.id.rvAdminUsers);
        adapter = new AdminUserAdapter(new AdminUserAdapter.UserActionListener() {
            @Override
            public void onHistory(User user) {
                openHistory(user);
            }

            @Override
            public void onCredit(User user) {
                openCreditDialog(user);
            }

            @Override
            public void onDelete(User user) {
                openDeleteDialog(user);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        adminRepository.observeUsers(liveData);
        liveData.observe(getViewLifecycleOwner(), users -> {
            allUsers.clear();
            if (users != null) allUsers.addAll(users);
            filter(etSearch.getText().toString());
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        String keyword = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            String haystack = (safe(user.getFullName()) + ' ' + safe(user.getEmail()) + ' ' + safe(user.getMobileNumber()) + ' ' + safe(user.getBloodGroup()) + ' ' + safe(user.getAddress())).toLowerCase(Locale.getDefault());
            if (keyword.isEmpty() || haystack.contains(keyword)) filtered.add(user);
        }
        adapter.submitList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openHistory(User user) {
        adminRepository.loadUserHistory(user.getUid(), new FirestoreCallback<AdminRepository.UserHistory>() {
            @Override
            public void onSuccess(AdminRepository.UserHistory data) {
                StringBuilder builder = new StringBuilder();
                builder.append("Requests created: ").append(data.requestsCount).append("\n");
                builder.append("Donation interests: ").append(data.donorInterestCount).append("\n");
                builder.append("Receiver-side histories: ").append(data.receiverHistoryCount).append("\n");
                builder.append("Credit entries: ").append(data.creditTransactionsCount).append("\n\n");
                if (!data.recentRequests.isEmpty()) {
                    builder.append("Recent requests:\n");
                    for (BloodRequest request : data.recentRequests) {
                        builder.append("• ").append(request.getPatientName()).append(" — ").append(request.getStatus()).append("\n");
                    }
                    builder.append('\n');
                }
                if (!data.recentDonations.isEmpty()) {
                    builder.append("Recent donation interests:\n");
                    for (Donation donation : data.recentDonations) {
                        builder.append("• ").append(donation.getRequestBloodGroup()).append(" — ").append(donation.getStatus()).append("\n");
                    }
                    builder.append('\n');
                }
                if (!data.recentTransactions.isEmpty()) {
                    builder.append("Recent credits:\n");
                    for (CreditTransaction transaction : data.recentTransactions) {
                        builder.append("• ").append(transaction.getPoints()).append(" points — ").append(transaction.getDescription()).append("\n");
                    }
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle(user.getFullName() + " history")
                        .setMessage(builder.toString())
                        .setPositiveButton("Close", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCreditDialog(User user) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 8, 32, 8);
        EditText pointsInput = new EditText(requireContext());
        pointsInput.setHint("Points, e.g. 10 or -10");
        pointsInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText reasonInput = new EditText(requireContext());
        reasonInput.setHint("Reason");
        layout.addView(pointsInput);
        layout.addView(reasonInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Adjust credits")
                .setView(layout)
                .setNegativeButton("Back", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String pointsText = pointsInput.getText().toString().trim();
                    String reason = reasonInput.getText().toString().trim();
                    if (pointsText.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter points to adjust", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int points = Integer.parseInt(pointsText);
                    adminRepository.adjustCredits(user, points, FirebaseAuth.getInstance().getUid(),
                            reason.isEmpty() ? "Admin balance adjustment" : reason,
                            toastCallback("Credits updated."));
                }).show();
    }

    private void openDeleteDialog(User user) {
        if ("admin".equals(user.getRole())) {
            Toast.makeText(requireContext(), "Admin accounts cannot be deleted from the app panel.", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText input = new EditText(requireContext());
        input.setHint("Deletion / blocking reason");
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete user")
                .setMessage("This uses a soft delete so history stays available for audit.")
                .setView(input)
                .setNegativeButton("Back", null)
                .setPositiveButton("Delete", (dialog, which) -> adminRepository.softDeleteUser(
                        user,
                        FirebaseAuth.getInstance().getUid(),
                        input.getText().toString().trim().isEmpty() ? "Blocked by admin" : input.getText().toString().trim(),
                        toastCallback("User blocked and hidden.")))
                .show();
    }

    private FirestoreCallback<Void> toastCallback(String successMessage) {
        return new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
