package com.redpulse.app.ui.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.repository.AdminRepository;
import com.redpulse.app.data.repository.RequestRepository;
import com.redpulse.app.ui.adapter.RequestAdapter;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;
import com.redpulse.app.utils.ValidationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminRequestsFragment extends Fragment {
    private static final String FILTER_PENDING = "pending";
    private static final String FILTER_LIVE = "live";
    private static final String FILTER_CLOSED = "closed";

    private final AdminRepository adminRepository = new AdminRepository();
    private final RequestRepository requestRepository = new RequestRepository();
    private final List<BloodRequest> allRequests = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private String currentFilter = FILTER_PENDING;
    private RequestAdapter adapter;
    private TextView tvEmpty;
    private TextView tvFilterSummary;
    private Button btnFilterPending;
    private Button btnFilterLive;
    private Button btnFilterClosed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvEmpty = view.findViewById(R.id.tvEmptyState);
        tvFilterSummary = view.findViewById(R.id.tvFilterSummary);
        btnFilterPending = view.findViewById(R.id.btnFilterPending);
        btnFilterLive = view.findViewById(R.id.btnFilterLive);
        btnFilterClosed = view.findViewById(R.id.btnFilterClosed);
        RecyclerView recyclerView = view.findViewById(R.id.rvAdminRequests);

        adapter = new RequestAdapter(RequestAdapter.Mode.ADMIN_MANAGE, new RequestAdapter.RequestActionListener() {
            @Override
            public void onPrimaryAction(BloodRequest request) {
                if (Constants.STATUS_PENDING_REVIEW.equals(request.getStatus())) {
                    showApprovalDialog(request);
                } else if (Constants.STATUS_PUBLISHED.equals(request.getStatus()) && request.isActive()) {
                    showEditDialog(request);
                }
            }

            @Override
            public void onSecondaryAction(BloodRequest request) {
                if (Constants.STATUS_PENDING_REVIEW.equals(request.getStatus())) {
                    showRejectDialog(request);
                } else if (Constants.STATUS_PUBLISHED.equals(request.getStatus()) && request.isActive()) {
                    showDeleteDialog(request);
                }
            }

            @Override
            public void onCardClicked(BloodRequest request) {
                Toast.makeText(requireContext(), request.getPatientName() + " • " + request.getContactNumber(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnFilterPending.setOnClickListener(v -> applyFilter(FILTER_PENDING));
        btnFilterLive.setOnClickListener(v -> applyFilter(FILTER_LIVE));
        btnFilterClosed.setOnClickListener(v -> applyFilter(FILTER_CLOSED));

        MutableLiveData<List<BloodRequest>> liveData = new MutableLiveData<>();
        adminRepository.observeAllRequests(liveData);
        liveData.observe(getViewLifecycleOwner(), requests -> {
            allRequests.clear();
            if (requests != null) allRequests.addAll(requests);
            applyFilter(currentFilter);
        });

        applyFilter(currentFilter);
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        List<BloodRequest> filtered = new ArrayList<>();
        for (BloodRequest request : allRequests) {
            if (FILTER_PENDING.equals(filter) && Constants.STATUS_PENDING_REVIEW.equals(request.getStatus())) {
                filtered.add(request);
            } else if (FILTER_LIVE.equals(filter) && Constants.STATUS_PUBLISHED.equals(request.getStatus()) && request.isActive()) {
                filtered.add(request);
            } else if (FILTER_CLOSED.equals(filter) && isClosedRequest(request)) {
                filtered.add(request);
            }
        }
        adapter.submitList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(resolveEmptyText(filter));
        tvFilterSummary.setText(filtered.size() + " request(s) in this view");

        btnFilterPending.setEnabled(!FILTER_PENDING.equals(filter));
        btnFilterLive.setEnabled(!FILTER_LIVE.equals(filter));
        btnFilterClosed.setEnabled(!FILTER_CLOSED.equals(filter));
    }

    private boolean isClosedRequest(BloodRequest request) {
        return Constants.STATUS_COMPLETED.equals(request.getStatus())
                || Constants.STATUS_CANCELLED.equals(request.getStatus())
                || Constants.STATUS_REJECTED.equals(request.getStatus())
                || (!request.isActive() && !Constants.STATUS_PENDING_REVIEW.equals(request.getStatus()));
    }

    private String resolveEmptyText(String filter) {
        if (FILTER_LIVE.equals(filter)) return "No live requests right now";
        if (FILTER_CLOSED.equals(filter)) return "No closed requests yet";
        return "No pending requests right now";
    }

    private void showApprovalDialog(BloodRequest request) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Optional internal note");
        new AlertDialog.Builder(requireContext())
                .setTitle("Approve request")
                .setMessage("Approving will publish the request to donors immediately.")
                .setView(editText)
                .setNegativeButton("Back", null)
                .setPositiveButton("Approve", (dialog, which) -> adminRepository.approveRequest(
                        request,
                        FirebaseAuth.getInstance().getUid(),
                        editText.getText().toString().trim(),
                        callbackForToast("Request approved and published.")))
                .show();
    }

    private void showRejectDialog(BloodRequest request) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Reason for rejection");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Reject request")
                .setView(editText)
                .setNegativeButton("Back", null)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = editText.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(requireContext(), "Please provide a rejection reason", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    adminRepository.rejectRequest(request, FirebaseAuth.getInstance().getUid(), reason,
                            callbackForToast("Request rejected."));
                })
                .show();
    }

    private void showDeleteDialog(BloodRequest request) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Why are you removing this live request?");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete live request")
                .setMessage("This will remove the request from the live feed for all donors.")
                .setView(editText)
                .setNegativeButton("Back", null)
                .setPositiveButton("Delete", (dialog, which) -> requestRepository.removeLiveRequestByAdmin(
                        request,
                        FirebaseAuth.getInstance().getUid(),
                        editText.getText().toString().trim(),
                        callbackForToast("Live request removed.")))
                .show();
    }

    private void showEditDialog(BloodRequest request) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_request_admin, null, false);
        EditText etPatientName = dialogView.findViewById(R.id.etEditPatientName);
        AutoCompleteTextView etBloodGroup = dialogView.findViewById(R.id.etEditBloodGroup);
        EditText etUnits = dialogView.findViewById(R.id.etEditRequiredUnits);
        EditText etHospitalName = dialogView.findViewById(R.id.etEditHospitalName);
        EditText etAddress = dialogView.findViewById(R.id.etEditFullAddress);
        EditText etContact = dialogView.findViewById(R.id.etEditContactNumber);
        EditText etDateTime = dialogView.findViewById(R.id.etEditRequiredDateTime);
        EditText etNotes = dialogView.findViewById(R.id.etEditAdditionalNotes);
        SwitchCompat switchEmergency = dialogView.findViewById(R.id.switchEditEmergency);

        etBloodGroup.setInputType(InputType.TYPE_NULL);
        etBloodGroup.setKeyListener(null);
        etBloodGroup.setOnClickListener(v -> etBloodGroup.showDropDown());
        etBloodGroup.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")));

        final long[] selectedMillis = {request.getRequiredTimestamp() != null
                ? request.getRequiredTimestamp().toDate().getTime()
                : System.currentTimeMillis() + 3600000L};

        etPatientName.setText(request.getPatientName());
        etBloodGroup.setText(request.getBloodGroup(), false);
        etUnits.setText(String.valueOf(request.getRequiredUnits()));
        etHospitalName.setText(request.getHospitalName());
        etAddress.setText(request.getFullAddress());
        etContact.setText(request.getContactNumber());
        etDateTime.setText(dateFormat.format(new Date(selectedMillis[0])));
        etNotes.setText(request.getNotes());
        switchEmergency.setChecked(request.isEmergency());

        etDateTime.setOnClickListener(v -> showDateTimePicker(selectedMillis, etDateTime));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit live request")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String patientName = textOf(etPatientName);
            String bloodGroup = ValidationUtils.normalizeBloodGroup(textOf(etBloodGroup));
            String unitsText = textOf(etUnits);
            String hospital = textOf(etHospitalName);
            String address = textOf(etAddress);
            String contact = textOf(etContact);

            if (TextUtils.isEmpty(patientName) || TextUtils.isEmpty(unitsText) || TextUtils.isEmpty(hospital)
                    || TextUtils.isEmpty(address) || TextUtils.isEmpty(contact)) {
                Toast.makeText(requireContext(), "Fill all mandatory fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidBloodGroup(bloodGroup)) {
                etBloodGroup.setError("Select a valid blood group");
                return;
            }
            if (!ValidationUtils.isValidPhone(contact)) {
                etContact.setError("Enter a valid contact number");
                return;
            }

            int units;
            try {
                units = Integer.parseInt(unitsText);
            } catch (NumberFormatException e) {
                etUnits.setError("Enter a valid unit number");
                return;
            }
            if (units <= 0) {
                etUnits.setError("Required units must be more than 0");
                return;
            }

            request.setPatientName(patientName);
            request.setBloodGroup(bloodGroup);
            request.setRequiredUnits(units);
            request.setHospitalName(hospital);
            request.setFullAddress(address);
            request.setContactNumber(contact);
            request.setRequiredTimestamp(new Timestamp(new Date(selectedMillis[0])));
            request.setNotes(textOf(etNotes));
            request.setEmergency(switchEmergency.isChecked());

            requestRepository.updateLiveRequestByAdmin(request, FirebaseAuth.getInstance().getUid(), new FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Toast.makeText(requireContext(), "Live request updated.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }));
        dialog.show();
    }

    private void showDateTimePicker(long[] selectedMillis, EditText etDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedMillis[0]);
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTimeInMillis(selectedMillis[0]);
            dateCal.set(Calendar.YEAR, year);
            dateCal.set(Calendar.MONTH, month);
            dateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                dateCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dateCal.set(Calendar.MINUTE, minute);
                dateCal.set(Calendar.SECOND, 0);
                selectedMillis[0] = dateCal.getTimeInMillis();
                etDateTime.setText(dateFormat.format(new Date(selectedMillis[0])));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String textOf(TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    private FirestoreCallback<Void> callbackForToast(String successMessage) {
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
}
