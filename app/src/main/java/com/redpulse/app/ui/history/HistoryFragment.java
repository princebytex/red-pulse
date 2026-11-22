package com.redpulse.app.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.HistoryItem;
import com.redpulse.app.data.repository.HistoryRepository;
import com.redpulse.app.ui.adapter.HistoryAdapter;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.List;

public class HistoryFragment extends Fragment {
    private final HistoryRepository repository = new HistoryRepository();
    private final HistoryAdapter adapter = new HistoryAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.rvHistory);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Please sign in again to load your history.");
            return;
        }

        repository.loadUserHistory(uid, new FirestoreCallback<List<HistoryItem>>() {
            @Override
            public void onSuccess(List<HistoryItem> data) {
                adapter.submitList(data);
                tvEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(message == null || message.trim().isEmpty()
                        ? "Could not load your activity history."
                        : message);
                Toast.makeText(requireContext(), tvEmpty.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
