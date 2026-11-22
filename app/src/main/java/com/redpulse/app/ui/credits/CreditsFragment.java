package com.redpulse.app.ui.credits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.UserRepository;
import com.redpulse.app.utils.Constants;

public class CreditsFragment extends Fragment {
    private final UserRepository repository = new UserRepository();
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvCreditsValue = view.findViewById(R.id.tvCreditsValue);
        TextView tvCreditsSubtitle = view.findViewById(R.id.tvCreditsSubtitle);
        TextView tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus);
        TextView tvCreditVerification = view.findViewById(R.id.tvCreditVerification);
        TextView tvRequestEligibility = view.findViewById(R.id.tvRequestEligibility);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            repository.observeUser(uid, userLiveData);
        }

        userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            tvCreditsValue.setText(String.valueOf(user.getTotalCredits()));
            tvAvailabilityStatus.setText(user.isAvailable() ? "Available for donation" : "Currently unavailable");
            tvCreditVerification.setText(user.isVerified() ? "Verified account" : "Pending verification");
            tvRequestEligibility.setText(user.getTotalCredits() >= Constants.REQUEST_CREDIT_COST
                    ? "You can create a blood request right now."
                    : "You need at least 1 credit before posting a request.");
            tvCreditsSubtitle.setText(user.getTotalCredits() >= Constants.REQUEST_CREDIT_COST
                    ? "A successful completion will transfer 1 credit from the requester to the donor."
                    : "Every new account starts with 1 bonus credit. Use it carefully for real blood requests.");
        });
    }
}
