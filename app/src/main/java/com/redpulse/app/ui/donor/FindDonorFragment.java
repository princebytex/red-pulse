package com.redpulse.app.ui.donor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.redpulse.app.R;
import com.redpulse.app.ui.adapter.DonorAdapter;
import com.redpulse.app.utils.ValidationUtils;

import java.util.Arrays;

public class FindDonorFragment extends Fragment {
    private final DonorAdapter donorAdapter = new DonorAdapter();
    private FusedLocationProviderClient locationClient;
    private Double userLat;
    private Double userLng;
    private TextView tvLocationHint;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_donor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DonorViewModel viewModel = new ViewModelProvider(this).get(DonorViewModel.class);
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        AutoCompleteTextView etBloodGroup = view.findViewById(R.id.etSearchBloodGroup);
        Button btnSearch = view.findViewById(R.id.btnSearchDonors);
        tvLocationHint = view.findViewById(R.id.tvLocationHint);
        RecyclerView rvDonors = view.findViewById(R.id.rvDonors);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyState);

        etBloodGroup.setInputType(InputType.TYPE_NULL);
        etBloodGroup.setKeyListener(null);
        etBloodGroup.setOnClickListener(v -> etBloodGroup.showDropDown());
        etBloodGroup.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")));

        rvDonors.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDonors.setAdapter(donorAdapter);

        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                fetchLocation();
            } else {
                tvLocationHint.setText("Location permission denied. Search will work without distance sorting.");
            }
        });

        fetchLocation();

        btnSearch.setOnClickListener(v ->
                viewModel.search(ValidationUtils.normalizeBloodGroup(etBloodGroup.getText().toString()), userLat, userLng));

        viewModel.getDonors().observe(getViewLifecycleOwner(), users -> {
            donorAdapter.submitList(users);
            tvEmpty.setVisibility(users == null || users.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            tvLocationHint.setText("Location permission needed for nearby donor sorting");
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                tvLocationHint.setText("Nearby sorting enabled");
            } else {
                tvLocationHint.setText("Location unavailable. Search will still work.");
            }
        }).addOnFailureListener(e -> tvLocationHint.setText("Location unavailable. Search will still work."));
    }
}
