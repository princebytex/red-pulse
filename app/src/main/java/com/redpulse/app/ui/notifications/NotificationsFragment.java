package com.redpulse.app.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.ui.adapter.NotificationAdapter;

public class NotificationsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NotificationsViewModel viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        NotificationAdapter adapter = new NotificationAdapter();
        RecyclerView recyclerView = view.findViewById(R.id.rvNotifications);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getNotifications().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            tvEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
        if (FirebaseAuth.getInstance().getUid() != null) {
            viewModel.observe(FirebaseAuth.getInstance().getUid());
        }
    }
}
