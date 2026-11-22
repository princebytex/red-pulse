package com.redpulse.app.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.redpulse.app.R;
import com.redpulse.app.data.model.User;
import com.redpulse.app.ui.history.HistoryFragment;
import com.redpulse.app.ui.main.MainActivity;
import com.redpulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private boolean bindingAvailability;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvBloodGroup = view.findViewById(R.id.tvProfileBloodGroup);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvPhone = view.findViewById(R.id.tvProfilePhone);
        TextView tvCredits = view.findViewById(R.id.tvProfileCredits);
        TextView tvVerification = view.findViewById(R.id.tvVerificationStatus);
        TextView tvAddress = view.findViewById(R.id.tvProfileAddress);
        TextView tvRequestAccess = view.findViewById(R.id.tvProfileRequestAccess);
        SwitchCompat switchAvailable = view.findViewById(R.id.switchAvailability);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        Button btnViewHistory = view.findViewById(R.id.btnViewHistory);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            viewModel.observeUser(uid);
        }

        viewModel.getUser().observe(getViewLifecycleOwner(),
                user -> bindUser(user, tvName, tvBloodGroup, tvEmail, tvPhone, tvCredits, tvVerification, tvAddress, tvRequestAccess, switchAvailable));

        switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (bindingAvailability) return;
            if (uid != null) viewModel.updateAvailability(uid, isChecked);
        });

        btnChangePassword.setOnClickListener(v -> showPasswordDialog());
        btnViewHistory.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new HistoryFragment(), true));
    }

    private void bindUser(User user, TextView tvName, TextView tvBloodGroup, TextView tvEmail, TextView tvPhone,
                          TextView tvCredits, TextView tvVerification, TextView tvAddress, TextView tvRequestAccess,
                          SwitchCompat switchAvailable) {
        if (user == null) return;
        tvName.setText(user.getFullName());
        tvBloodGroup.setText(user.getBloodGroup());
        tvEmail.setText(user.getEmail());
        tvPhone.setText(user.getMobileNumber());
        tvCredits.setText(String.valueOf(user.getTotalCredits()));
        tvVerification.setText(user.isBlocked()
                ? "Blocked account"
                : user.isVerified() ? "Verified" : "Pending verification");
        tvAddress.setText(buildProfileAddress(user));
        tvRequestAccess.setText(user.getTotalCredits() >= Constants.REQUEST_CREDIT_COST
                ? "You can post a blood request right now."
                : "You need at least 1 credit before posting a blood request.");
        bindingAvailability = true;
        switchAvailable.setChecked(user.isAvailable());
        switchAvailable.setEnabled(!user.isBlocked());
        bindingAvailability = false;
    }

    private void showPasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null, false);
        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Change password")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Update", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText() == null ? "" : etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText() == null ? "" : etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Enter current password");
                return;
            }
            if (newPassword.length() < 6) {
                etNewPassword.setError("New password must be at least 6 characters");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null || firebaseUser.getEmail() == null) {
                Toast.makeText(requireContext(), "Please sign in again to change your password.", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
            firebaseUser.reauthenticate(credential)
                    .addOnSuccessListener(unused -> firebaseUser.updatePassword(newPassword)
                            .addOnSuccessListener(passwordUnused -> {
                                Toast.makeText(requireContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(error -> Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(error -> Toast.makeText(requireContext(), "Current password is incorrect.", Toast.LENGTH_SHORT).show());
        }));
        dialog.show();
    }

    private String buildProfileAddress(User user) {
        if (user.getAddress() != null && !user.getAddress().trim().isEmpty()) {
            return user.getAddress().trim();
        }
        List<String> parts = new ArrayList<>();
        addPart(parts, user.getAddressLine());
        if (user.getUnionName() != null && !Constants.UNION_NOT_APPLICABLE.equalsIgnoreCase(user.getUnionName())) {
            addPart(parts, user.getUnionName());
        }
        addPart(parts, user.getUpazila());
        if (user.getCityCorporation() != null && !Constants.CITY_CORPORATION_NONE.equalsIgnoreCase(user.getCityCorporation())) {
            addPart(parts, user.getCityCorporation());
        }
        addPart(parts, user.getZila());
        if (user.getDistrict() != null && (user.getZila() == null || !user.getDistrict().equalsIgnoreCase(user.getZila()))) {
            addPart(parts, user.getDistrict());
        }
        addPart(parts, user.getDivision());
        if (parts.isEmpty()) return "Address not added yet";
        return android.text.TextUtils.join(", ", parts);
    }

    private void addPart(List<String> parts, String value) {
        if (value != null && !value.trim().isEmpty()) parts.add(value.trim());
    }
}
