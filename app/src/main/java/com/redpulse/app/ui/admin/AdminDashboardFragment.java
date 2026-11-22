package com.redpulse.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.redpulse.app.R;
import com.redpulse.app.data.model.AdminNotice;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.AdminRepository;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.ui.main.MainActivity;
import com.redpulse.app.utils.Constants;

import java.util.List;

public class AdminDashboardFragment extends Fragment {
    private final AdminRepository adminRepository = new AdminRepository();
    private final UserRepository userRepository = new UserRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvPendingCount = view.findViewById(R.id.tvPendingCount);
        TextView tvLiveCount = view.findViewById(R.id.tvLiveCount);
        TextView tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        TextView tvUserCount = view.findViewById(R.id.tvUserCount);
        TextView tvNoticeCount = view.findViewById(R.id.tvNoticeCount);
        TextView tvAdminSummary = view.findViewById(R.id.tvAdminSummary);
        Button btnGoAdminRequests = view.findViewById(R.id.btnGoAdminRequests);
        Button btnGoAdminUsers = view.findViewById(R.id.btnGoAdminUsers);
        Button btnGoAdminNotices = view.findViewById(R.id.btnGoAdminNotices);

        MutableLiveData<List<BloodRequest>> requestsLiveData = new MutableLiveData<>();
        MutableLiveData<List<User>> userLiveData = new MutableLiveData<>();
        MutableLiveData<List<AdminNotice>> noticeLiveData = new MutableLiveData<>();

        adminRepository.observeAllRequests(requestsLiveData);
        userRepository.observeAllUsers(userLiveData);
        adminRepository.observeNotices(noticeLiveData);

        requestsLiveData.observe(getViewLifecycleOwner(), requests -> {
            int pending = 0;
            int live = 0;
            int completed = 0;
            if (requests != null) {
                for (BloodRequest request : requests) {
                    if (Constants.STATUS_PENDING_REVIEW.equals(request.getStatus())) pending++;
                    if (Constants.STATUS_PUBLISHED.equals(request.getStatus()) && request.isActive()) live++;
                    if (Constants.STATUS_COMPLETED.equals(request.getStatus())) completed++;
                }
            }
            tvPendingCount.setText(String.valueOf(pending));
            tvLiveCount.setText(String.valueOf(live));
            tvCompletedCount.setText(String.valueOf(completed));
            tvAdminSummary.setText(live > 0
                    ? live + " live request(s) are currently visible to donors."
                    : "No live requests are currently running. Review pending cases to publish more.");
        });

        userLiveData.observe(getViewLifecycleOwner(), users -> tvUserCount.setText(String.valueOf(users == null ? 0 : users.size())));

        noticeLiveData.observe(getViewLifecycleOwner(), notices -> {
            int activeCount = 0;
            if (notices != null) {
                for (AdminNotice notice : notices) {
                    if (notice.isActive()) activeCount++;
                }
            }
            tvNoticeCount.setText(String.valueOf(activeCount));
        });

        btnGoAdminRequests.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new AdminRequestsFragment(), true));
        btnGoAdminUsers.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new AdminUsersFragment(), true));
        btnGoAdminNotices.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new AdminNoticesFragment(), true));
    }
}
