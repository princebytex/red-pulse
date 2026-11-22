package com.redpulse.app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.DonationRepository;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.ui.adapter.NoticeAdapter;
import com.redpulse.app.ui.adapter.RequestAdapter;
import com.redpulse.app.ui.credits.CreditsFragment;
import com.redpulse.app.ui.donor.FindDonorFragment;
import com.redpulse.app.ui.history.HistoryFragment;
import com.redpulse.app.ui.main.MainActivity;
import com.redpulse.app.ui.request.CreateRequestFragment;
import com.redpulse.app.ui.request.RequestFeedFragment;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private final NoticeAdapter noticeAdapter = new NoticeAdapter();
    private RequestAdapter requestAdapter;
    private final UserRepository userRepository = new UserRepository();
    private final DonationRepository donationRepository = new DonationRepository();
    private User currentUser;
    private TextView tvCredits;
    private TextView tvAvailability;
    private TextView tvHeroCaption;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        RecyclerView rvNotices = view.findViewById(R.id.rvNotices);
        RecyclerView rvEmergency = view.findViewById(R.id.rvEmergencyRequests);
        TextView tvNoticeStrip = view.findViewById(R.id.tvNoticeStrip);
        tvCredits = view.findViewById(R.id.tvHomeCredits);
        tvAvailability = view.findViewById(R.id.tvHomeAvailability);
        tvHeroCaption = view.findViewById(R.id.tvHeroCaption);

        MaterialCardView cardRequest = view.findViewById(R.id.cardRequestBlood);
        MaterialCardView cardEmergency = view.findViewById(R.id.cardEmergency);
        MaterialCardView cardDonor = view.findViewById(R.id.cardFindDonor);
        MaterialCardView cardCredits = view.findViewById(R.id.cardCredits);
        MaterialCardView cardHistory = view.findViewById(R.id.cardHistory);
        MaterialCardView cardRewards = view.findViewById(R.id.cardRewards);

        requestAdapter = new RequestAdapter(RequestAdapter.Mode.FEED, new RequestAdapter.RequestActionListener() {
            @Override
            public void onPrimaryAction(BloodRequest request) {
                expressInterest(request);
            }

            @Override
            public void onSecondaryAction(BloodRequest request) {
            }

            @Override
            public void onCardClicked(BloodRequest request) {
                Toast.makeText(requireContext(), request.getHospitalName() + " • " + request.getContactNumber(), Toast.LENGTH_SHORT).show();
            }
        });

        rvNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotices.setAdapter(noticeAdapter);
        rvEmergency.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEmergency.setAdapter(requestAdapter);

        cardRequest.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new CreateRequestFragment(), true));
        cardEmergency.setOnClickListener(v -> {
            CreateRequestFragment fragment = new CreateRequestFragment();
            Bundle args = new Bundle();
            args.putBoolean("forceEmergency", true);
            fragment.setArguments(args);
            ((MainActivity) requireActivity()).openFragment(fragment, true);
        });
        cardDonor.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new FindDonorFragment(), true));
        cardCredits.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new CreditsFragment(), true));
        cardHistory.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new HistoryFragment(), true));
        cardRewards.setOnClickListener(v -> ((MainActivity) requireActivity()).openFragment(new RequestFeedFragment(), true));

        viewModel.getNotices().observe(getViewLifecycleOwner(), notices -> {
            noticeAdapter.submitList(notices);
            if (notices != null && !notices.isEmpty()) {
                tvNoticeStrip.setText(notices.get(0).getTitle() + " • " + notices.get(0).getMessage());
            } else {
                tvNoticeStrip.setText("Stay ready. Live notices from admins will appear here.");
            }
            tvNoticeStrip.setSelected(true);
        });

        viewModel.getHomeRequests().observe(getViewLifecycleOwner(), requestAdapter::submitList);
        preloadUser();
        viewModel.loadHomeData(FirebaseAuth.getInstance().getUid());
    }

    private void preloadUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        userRepository.fetchUser(uid, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User data) {
                currentUser = data;
                tvCredits.setText(String.valueOf(data.getTotalCredits()));
                tvAvailability.setText(data.isAvailable() ? "Available for donation" : "Currently unavailable");
                tvHeroCaption.setText(data.getTotalCredits() >= Constants.REQUEST_CREDIT_COST
                        ? "You are eligible to post a blood request and manage donor contacts professionally."
                        : "You need at least 1 credit to post a blood request.");
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void expressInterest(BloodRequest request) {
        if (currentUser == null) {
            preloadUser();
            Toast.makeText(requireContext(), "Loading your profile, please tap again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser.isBlocked() || currentUser.isDeleted()) {
            Toast.makeText(requireContext(), "Your account cannot submit donor interest right now.", Toast.LENGTH_LONG).show();
            return;
        }
        donationRepository.expressInterest(request, currentUser, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(requireContext(), "Interest submitted. The requester can now view your contact details.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
