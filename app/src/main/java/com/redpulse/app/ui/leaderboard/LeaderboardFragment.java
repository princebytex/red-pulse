package com.redpulse.app.ui.leaderboard;

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
import com.redpulse.app.R;
import com.redpulse.app.ui.adapter.LeaderboardAdapter;

public class LeaderboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LeaderboardViewModel viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        LeaderboardAdapter adapter = new LeaderboardAdapter();
        RecyclerView recyclerView = view.findViewById(R.id.rvLeaderboard);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getLeaderboard().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            tvEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.load();
    }
}
