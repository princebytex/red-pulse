package com.redpulse.app.ui.request;

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

import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.DonationRepository;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.ui.adapter.RequestAdapter;
import com.redpulse.app.utils.FirestoreCallback;

public class RequestFeedFragment extends Fragment {
    private final UserRepository userRepository = new UserRepository();
    private final DonationRepository donationRepository = new DonationRepository();
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RequestViewModel viewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyState);
        RecyclerView recyclerView = view.findViewById(R.id.rvFeedRequests);
        RequestAdapter adapter = new RequestAdapter(RequestAdapter.Mode.FEED, new RequestAdapter.RequestActionListener() {
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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getFeedRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter.submitList(requests);
            tvEmpty.setVisibility(requests == null || requests.isEmpty() ? View.VISIBLE : View.GONE);
        });

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            viewModel.observeFeed(uid);
            userRepository.fetchUser(uid, new FirestoreCallback<User>() {
                @Override
                public void onSuccess(User data) {
                    currentUser = data;
                }

                @Override
                public void onError(String message) {
                }
            });
        }
    }

    private void expressInterest(BloodRequest request) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Profile still loading, please retry.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser.isBlocked() || currentUser.isDeleted()) {
            Toast.makeText(requireContext(), "Your account cannot submit donor interest right now.", Toast.LENGTH_LONG).show();
            return;
        }
        donationRepository.expressInterest(request, currentUser, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(requireContext(), "Interest submitted successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
