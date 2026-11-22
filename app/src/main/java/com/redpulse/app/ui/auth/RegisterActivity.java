package com.redpulse.app.ui.auth;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.redpulse.app.R;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.AddressRepository;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;
import com.redpulse.app.utils.ValidationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etAge;
    private EditText etAddressLine;
    private EditText etEmail;
    private EditText etMobile;
    private EditText etPassword;
    private AutoCompleteTextView actBloodGroup;
    private AutoCompleteTextView actDivision;
    private AutoCompleteTextView actDistrict;
    private AutoCompleteTextView actZila;
    private AutoCompleteTextView actCityCorporation;
    private AutoCompleteTextView actUpazila;
    private AutoCompleteTextView actUnion;
    private ProgressBar progressBar;
    private Button btnRegister;

    private final UserRepository userRepository = new UserRepository();
    private final AddressRepository addressRepository = new AddressRepository();

    private AddressRepository.Catalog catalog;
    private AddressRepository.Division selectedDivision;
    private AddressRepository.District selectedDistrict;
    private AddressRepository.Upazila selectedUpazila;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        configureDropdown(actBloodGroup);
        configureDropdown(actDivision);
        configureDropdown(actDistrict);
        configureDropdown(actZila);
        configureDropdown(actCityCorporation);
        configureDropdown(actUpazila);
        configureDropdown(actUnion);

        setDropdownOptions(actBloodGroup, Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        actBloodGroup.setOnItemClickListener((parent, view, position, id) -> actBloodGroup.setError(null));
        actDivision.setOnItemClickListener((parent, view, position, id) -> onDivisionSelected(String.valueOf(parent.getItemAtPosition(position))));
        actDistrict.setOnItemClickListener((parent, view, position, id) -> onDistrictSelected(String.valueOf(parent.getItemAtPosition(position))));
        actUpazila.setOnItemClickListener((parent, view, position, id) -> onUpazilaSelected(String.valueOf(parent.getItemAtPosition(position))));
        actZila.setOnItemClickListener((parent, view, position, id) -> actZila.setError(null));
        actCityCorporation.setOnItemClickListener((parent, view, position, id) -> actCityCorporation.setError(null));
        actUnion.setOnItemClickListener((parent, view, position, id) -> actUnion.setError(null));

        btnRegister.setOnClickListener(v -> register());
        loadAddressCatalog();
    }

    private void bindViews() {
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddressLine = findViewById(R.id.etAddressLine);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        actBloodGroup = findViewById(R.id.actBloodGroup);
        actDivision = findViewById(R.id.actDivision);
        actDistrict = findViewById(R.id.actDistrict);
        actZila = findViewById(R.id.actZila);
        actCityCorporation = findViewById(R.id.actCityCorporation);
        actUpazila = findViewById(R.id.actUpazila);
        actUnion = findViewById(R.id.actUnion);
        progressBar = findViewById(R.id.progressBar);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void configureDropdown(@NonNull AutoCompleteTextView view) {
        view.setInputType(InputType.TYPE_NULL);
        view.setKeyListener(null);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) view.showDropDown();
        });
    }

    private void loadAddressCatalog() {
        setAddressInputsEnabled(false);
        new Thread(() -> {
            try {
                AddressRepository.Catalog loadedCatalog = addressRepository.getCatalog(this);
                runOnUiThread(() -> {
                    catalog = loadedCatalog;
                    setDropdownOptions(actDivision, catalog.getDivisionNames());
                    setAddressInputsEnabled(true);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    setAddressInputsEnabled(true);
                    Toast.makeText(this, "Failed to load Bangladesh address data", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void setAddressInputsEnabled(boolean enabled) {
        actDivision.setEnabled(enabled);
        actDistrict.setEnabled(enabled);
        actZila.setEnabled(enabled);
        actCityCorporation.setEnabled(enabled);
        actUpazila.setEnabled(enabled);
        actUnion.setEnabled(enabled);
    }

    private void setDropdownOptions(@NonNull AutoCompleteTextView view, @NonNull List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);
        view.setAdapter(adapter);
    }

    private void onDivisionSelected(String divisionName) {
        if (catalog == null) return;
        selectedDivision = catalog.findDivision(divisionName);
        selectedDistrict = null;
        selectedUpazila = null;

        actDivision.setError(null);
        actDistrict.setText("", false);
        actZila.setText("", false);
        actCityCorporation.setText("", false);
        actUpazila.setText("", false);
        actUnion.setText("", false);

        List<String> districtNames = selectedDivision == null
                ? Collections.emptyList()
                : mapDistrictNames(selectedDivision.getDistricts());
        setDropdownOptions(actDistrict, districtNames);
        setDropdownOptions(actZila, Collections.emptyList());
        setDropdownOptions(actCityCorporation, Collections.emptyList());
        setDropdownOptions(actUpazila, Collections.emptyList());
        setDropdownOptions(actUnion, Collections.emptyList());
    }

    private void onDistrictSelected(String districtName) {
        selectedDistrict = null;
        selectedUpazila = null;
        if (selectedDivision != null) {
            for (AddressRepository.District district : selectedDivision.getDistricts()) {
                if (district.getName().equalsIgnoreCase(districtName)) {
                    selectedDistrict = district;
                    break;
                }
            }
        }

        actDistrict.setError(null);
        actZila.setText("", false);
        actCityCorporation.setText("", false);
        actUpazila.setText("", false);
        actUnion.setText("", false);

        if (selectedDistrict == null) {
            setDropdownOptions(actZila, Collections.emptyList());
            setDropdownOptions(actCityCorporation, Collections.emptyList());
            setDropdownOptions(actUpazila, Collections.emptyList());
            setDropdownOptions(actUnion, Collections.emptyList());
            return;
        }

        setDropdownOptions(actZila, Collections.singletonList(selectedDistrict.getZilaName()));
        actZila.setText(selectedDistrict.getZilaName(), false);
        setDropdownOptions(actCityCorporation, selectedDistrict.getCityCorporations());
        if (!selectedDistrict.getCityCorporations().isEmpty()) {
            actCityCorporation.setText(selectedDistrict.getCityCorporations().get(0), false);
        }
        setDropdownOptions(actUpazila, mapUpazilaNames(selectedDistrict.getUpazilas()));
        setDropdownOptions(actUnion, Collections.singletonList(Constants.UNION_NOT_APPLICABLE));
        actUnion.setText(Constants.UNION_NOT_APPLICABLE, false);
    }

    private void onUpazilaSelected(String upazilaName) {
        selectedUpazila = null;
        if (selectedDistrict != null) {
            for (AddressRepository.Upazila upazila : selectedDistrict.getUpazilas()) {
                if (upazila.getName().equalsIgnoreCase(upazilaName)) {
                    selectedUpazila = upazila;
                    break;
                }
            }
        }
        actUpazila.setError(null);
        actUnion.setText("", false);
        if (selectedUpazila == null) {
            setDropdownOptions(actUnion, Collections.emptyList());
            return;
        }
        setDropdownOptions(actUnion, selectedUpazila.getUnions());
        if (!selectedUpazila.getUnions().isEmpty()) {
            actUnion.setText(selectedUpazila.getUnions().get(0), false);
        }
    }

    private List<String> mapDistrictNames(List<AddressRepository.District> districts) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        for (AddressRepository.District district : districts) names.add(district.getName());
        return names;
    }

    private List<String> mapUpazilaNames(List<AddressRepository.Upazila> upazilas) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        for (AddressRepository.Upazila upazila : upazilas) names.add(upazila.getName());
        return names;
    }

    private void register() {
        String name = safeText(etName);
        String ageText = safeText(etAge);
        String bloodGroup = ValidationUtils.normalizeBloodGroup(actBloodGroup.getText().toString());
        String division = safeText(actDivision);
        String district = safeText(actDistrict);
        String zila = safeText(actZila);
        String cityCorporation = safeText(actCityCorporation);
        String upazila = safeText(actUpazila);
        String unionName = safeText(actUnion);
        String addressLine = safeText(etAddressLine);
        String email = safeText(etEmail);
        String mobile = safeText(etMobile);
        String password = safeText(etPassword);

        if (!ValidationUtils.isNonEmpty(name)) {
            etName.setError("Enter your full name");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException error) {
            etAge.setError("Enter a valid age");
            return;
        }
        if (age < 16 || age > 80) {
            etAge.setError("Age should be between 16 and 80");
            return;
        }
        if (!ValidationUtils.isValidBloodGroup(bloodGroup)) {
            actBloodGroup.setError("Select a valid blood group");
            return;
        }
        if (catalog == null) {
            Toast.makeText(this, "Address data is still loading. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (division.isEmpty()) {
            actDivision.setError("Select division");
            return;
        }
        if (district.isEmpty()) {
            actDistrict.setError("Select district");
            return;
        }
        if (zila.isEmpty()) {
            actZila.setError("Select zila");
            return;
        }
        if (cityCorporation.isEmpty()) {
            actCityCorporation.setError("Select city corporation");
            return;
        }
        if (upazila.isEmpty()) {
            actUpazila.setError("Select upazila");
            return;
        }
        if (unionName.isEmpty()) {
            actUnion.setError("Select union");
            return;
        }
        if (!ValidationUtils.isNonEmpty(addressLine)) {
            etAddressLine.setError("Enter the rest of your address");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Invalid email address");
            return;
        }
        if (!ValidationUtils.isValidPhone(mobile)) {
            etMobile.setError("Enter a valid mobile number");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        String fullAddress = buildFullAddress(addressLine, unionName, upazila, cityCorporation, zila, district, division);
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        onRegistrationFinished();
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User user = new User();
                    user.setUid(firebaseUser.getUid());
                    user.setFullName(name);
                    user.setAge(age);
                    user.setBloodGroup(bloodGroup);
                    user.setAddress(fullAddress);
                    user.setAddressLine(addressLine);
                    user.setDivision(division);
                    user.setDistrict(district);
                    user.setZila(zila);
                    user.setCityCorporation(cityCorporation);
                    user.setUpazila(upazila);
                    user.setUnionName(unionName);
                    user.setEmail(email);
                    user.setMobileNumber(mobile);
                    user.setRole(Constants.ROLE_USER);

                    userRepository.createUserProfile(user, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            firebaseUser.sendEmailVerification();
                            onRegistrationFinished();
                            Toast.makeText(RegisterActivity.this,
                                    "Account created successfully. Please verify your email before login.",
                                    Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            firebaseUser.delete();
                            onRegistrationFinished();
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(error -> {
                    onRegistrationFinished();
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onRegistrationFinished() {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
    }

    private String buildFullAddress(String addressLine, String unionName, String upazila,
                                    String cityCorporation, String zila, String district, String division) {
        StringBuilder builder = new StringBuilder();
        appendAddressPart(builder, addressLine);
        if (!Constants.UNION_NOT_APPLICABLE.equalsIgnoreCase(unionName)) appendAddressPart(builder, unionName);
        appendAddressPart(builder, upazila);
        if (!Constants.CITY_CORPORATION_NONE.equalsIgnoreCase(cityCorporation)) appendAddressPart(builder, cityCorporation);
        appendAddressPart(builder, zila);
        if (!district.equalsIgnoreCase(zila)) appendAddressPart(builder, district);
        appendAddressPart(builder, division);
        return builder.toString();
    }

    private void appendAddressPart(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (builder.length() > 0) builder.append(", ");
        builder.append(value.trim());
    }

    private String safeText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String safeText(AutoCompleteTextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }
}
