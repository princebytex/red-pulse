package com.redpulse.app.ui.request;

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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.RequestRepository;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.utils.FirestoreCallback;
import com.redpulse.app.utils.ValidationUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateRequestFragment extends Fragment {
    private final RequestRepository repository = new RequestRepository();
    private final UserRepository userRepository = new UserRepository();
    private EditText etPatientName;
    private EditText etUnits;
    private EditText etHospitalName;
    private EditText etAddress;
    private EditText etContact;
    private EditText etDateTime;
    private EditText etNotes;
    private AutoCompleteTextView etBloodGroup;
    private SwitchCompat switchEmergency;
    private TextView tvRequestCreditHint;
    private long selectedMillis = System.currentTimeMillis() + 3600000;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etPatientName = view.findViewById(R.id.etPatientName);
        etBloodGroup = view.findViewById(R.id.etBloodGroup);
        etUnits = view.findViewById(R.id.etRequiredUnits);
        etHospitalName = view.findViewById(R.id.etHospitalName);
        etAddress = view.findViewById(R.id.etFullAddress);
        etContact = view.findViewById(R.id.etContactNumber);
        etDateTime = view.findViewById(R.id.etRequiredDateTime);
        etNotes = view.findViewById(R.id.etAdditionalNotes);
        switchEmergency = view.findViewById(R.id.switchEmergency);
        tvRequestCreditHint = view.findViewById(R.id.tvRequestCreditHint);
        Button btnSubmit = view.findViewById(R.id.btnSubmitRequest);

        etBloodGroup.setInputType(InputType.TYPE_NULL);
        etBloodGroup.setKeyListener(null);
        etBloodGroup.setOnClickListener(v -> etBloodGroup.showDropDown());
        etBloodGroup.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etBloodGroup.showDropDown();
        });
        etBloodGroup.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")));

        if (getArguments() != null && getArguments().getBoolean("forceEmergency", false)) {
            switchEmergency.setChecked(true);
        }

        etDateTime.setText(dateFormat.format(new Date(selectedMillis)));
        etDateTime.setOnClickListener(v -> pickDateTime());
        btnSubmit.setOnClickListener(v -> submitRequest());
        preloadUser();
    }

    private void preloadUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        userRepository.fetchUser(uid, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User data) {
                currentUser = data;
                if (etContact.getText().toString().trim().isEmpty()) {
                    etContact.setText(data.getMobileNumber());
                }
                if (etAddress.getText().toString().trim().isEmpty() && data.getAddress() != null) {
                    etAddress.setText(data.getAddress());
                }
                if (tvRequestCreditHint != null) {
                    tvRequestCreditHint.setText(data.getTotalCredits() >= 1
                            ? "You have enough credit to submit a request. 1 credit will be transferred to the donor after completion."
                            : "You need at least 1 credit before submitting a blood request.");
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void pickDateTime() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar dateCal = Calendar.getInstance();
            dateCal.set(Calendar.YEAR, year);
            dateCal.set(Calendar.MONTH, month);
            dateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                dateCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dateCal.set(Calendar.MINUTE, minute);
                dateCal.set(Calendar.SECOND, 0);
                selectedMillis = dateCal.getTimeInMillis();
                etDateTime.setText(dateFormat.format(new Date(selectedMillis)));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submitRequest() {
        if (currentUser != null && (currentUser.isBlocked() || currentUser.isDeleted())) {
            Toast.makeText(requireContext(), "Your account cannot create requests right now.", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentUser != null && currentUser.getTotalCredits() < 1) {
            Toast.makeText(requireContext(), "You need at least 1 credit before submitting a blood request.", Toast.LENGTH_LONG).show();
            return;
        }

        String patient = etPatientName.getText().toString().trim();
        String bloodGroup = ValidationUtils.normalizeBloodGroup(etBloodGroup.getText().toString());
        String unitsText = etUnits.getText().toString().trim();
        String hospital = etHospitalName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(patient) || TextUtils.isEmpty(unitsText)
                || TextUtils.isEmpty(hospital) || TextUtils.isEmpty(address) || TextUtils.isEmpty(contact)) {
            Toast.makeText(requireContext(), "Please fill all mandatory fields", Toast.LENGTH_SHORT).show();
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
        if (selectedMillis < System.currentTimeMillis() - 60000) {
            Toast.makeText(requireContext(), "Please choose a future date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        BloodRequest request = new BloodRequest();
        request.setRequesterId(FirebaseAuth.getInstance().getUid());
        request.setRequesterName(currentUser != null ? currentUser.getFullName() : (FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "User"));
        request.setRequesterPhone(currentUser != null ? currentUser.getMobileNumber() : contact);
        request.setPatientName(patient);
        request.setBloodGroup(bloodGroup);
        request.setRequiredUnits(units);
        request.setHospitalName(hospital);
        request.setFullAddress(address);
        request.setContactNumber(contact);
        request.setRequiredTimestamp(new Timestamp(new Date(selectedMillis)));
        request.setEmergency(switchEmergency.isChecked());
        request.setNotes(notes);

        repository.createRequest(request, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String data) {
                Toast.makeText(requireContext(),
                        request.isEmergency()
                                ? "Emergency request sent for admin approval"
                                : "Blood request submitted for admin approval",
                        Toast.LENGTH_LONG).show();
                clearForm();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etPatientName.setText("");
        etBloodGroup.setText("", false);
        etUnits.setText("");
        etHospitalName.setText("");
        if (currentUser != null && currentUser.getAddress() != null) {
            etAddress.setText(currentUser.getAddress());
        } else {
            etAddress.setText("");
        }
        selectedMillis = System.currentTimeMillis() + 3600000;
        etDateTime.setText(dateFormat.format(new Date(selectedMillis)));
        etNotes.setText("");
        switchEmergency.setChecked(false);
        if (currentUser != null) etContact.setText(currentUser.getMobileNumber());
    }
}
