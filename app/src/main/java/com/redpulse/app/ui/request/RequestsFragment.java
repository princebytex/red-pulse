package com.redpulse.app.ui.request;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.redpulse.app.data.model.Donation;
import com.redpulse.app.data.repository.DonationRepository;
import com.redpulse.app.data.repository.RequestRepository;
import com.redpulse.app.ui.adapter.DonationInterestAdapter;
import com.redpulse.app.ui.adapter.RequestAdapter;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.List;

public class RequestsFragment extends Fragment {
    private final RequestRepository requestRepository = new RequestRepository();
    private final DonationRepository donationRepository = new DonationRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RequestViewModel viewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        RequestAdapter adapter = new RequestAdapter(RequestAdapter.Mode.MY_REQUESTS, new RequestAdapter.RequestActionListener() {
            @Override
            public void onPrimaryAction(BloodRequest request) {
                showInterestedDonors(request);
            }

            @Override
            public void onSecondaryAction(BloodRequest request) {
                showDeleteDialog(request);
            }

            @Override
            public void onCardClicked(BloodRequest request) {
                Toast.makeText(requireContext(), request.getHospitalName() + " • " + request.getContactNumber(), Toast.LENGTH_SHORT).show();
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.rvRequests);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter.submitList(requests);
            tvEmpty.setVisibility(requests == null || requests.isEmpty() ? View.VISIBLE : View.GONE);
        });

        if (FirebaseAuth.getInstance().getUid() != null) {
            viewModel.observeMyRequests(FirebaseAuth.getInstance().getUid());
        }
    }

    private void showDeleteDialog(BloodRequest request) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Why are you cancelling this request?");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel request")
                .setView(editText)
                .setNegativeButton("Back", null)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = editText.getText().toString().trim();
                    if (reason.isEmpty()) reason = "Cancelled by requester";
                    requestRepository.cancelRequest(request, reason, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Toast.makeText(requireContext(), "Request removed from the live feed.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }).show();
    }

    private void showInterestedDonors(BloodRequest request) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_request_interests, null, false);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogInterestTitle);
        TextView tvEmpty = dialogView.findViewById(R.id.tvDialogInterestEmpty);
        RecyclerView recyclerView = dialogView.findViewById(R.id.rvDialogInterests);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .create();

        final DonationInterestAdapter[] adapterHolder = new DonationInterestAdapter[1];
        adapterHolder[0] = new DonationInterestAdapter(new DonationInterestAdapter.InterestActionListener() {
            @Override
            public void onConfirmDonation(Donation donation) {
                confirmDonation(request, donation, dialog::dismiss);
            }

            @Override
            public void onCancelDonation(Donation donation) {
                cancelDonationInterest(request, donation, () -> reloadInterests(request, adapterHolder[0], tvEmpty));
            }
        });
        recyclerView.setAdapter(adapterHolder[0]);

        tvTitle.setText("Interested donors • " + request.getPatientName());
        dialog.show();
        reloadInterests(request, adapterHolder[0], tvEmpty);
    }

    private void reloadInterests(BloodRequest request, DonationInterestAdapter adapter, TextView tvEmpty) {
        donationRepository.loadInterestsForRequestOnce(request.getId(), new FirestoreCallback<List<Donation>>() {
            @Override
            public void onSuccess(List<Donation> data) {
                adapter.submitList(data);
                boolean empty = data == null || data.isEmpty();
                tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                if (empty) {
                    tvEmpty.setText("No donor interest yet. Contact details will appear here when donors respond.");
                }
            }

            @Override
            public void onError(String message) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(message == null || message.trim().isEmpty()
                        ? "Could not load donor responses right now."
                        : message);
            }
        });
    }

    private void confirmDonation(BloodRequest request, Donation donation, Runnable onSuccess) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Add a short note (optional)");
        new AlertDialog.Builder(requireContext())
                .setTitle("Mark blood given")
                .setMessage("This will close the request and transfer 1 credit from the requester account to the donor.")
                .setView(editText)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (dialog, which) -> donationRepository.confirmDonation(
                        request,
                        donation,
                        editText.getText().toString().trim().isEmpty() ? "Donation confirmed by receiver" : editText.getText().toString().trim(),
                        new FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                Toast.makeText(requireContext(), "Donation confirmed. Credit transfer will complete automatically.", Toast.LENGTH_LONG).show();
                                if (onSuccess != null) onSuccess.run();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }

    private void cancelDonationInterest(BloodRequest request, Donation donation, Runnable onSuccess) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Reason for cancelling this donor (optional)");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel donor")
                .setMessage("The donor will no longer be shown as an active response for this request.")
                .setView(editText)
                .setNegativeButton("Back", null)
                .setPositiveButton("Cancel donor", (dialog, which) -> donationRepository.cancelDonationInterest(
                        request,
                        donation,
                        editText.getText().toString().trim(),
                        new FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                Toast.makeText(requireContext(), "Donor response cancelled.", Toast.LENGTH_SHORT).show();
                                if (onSuccess != null) onSuccess.run();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }
}
