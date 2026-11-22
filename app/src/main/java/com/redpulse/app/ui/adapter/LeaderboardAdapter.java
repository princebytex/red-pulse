package com.redpulse.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.redpulse.app.R;
import com.redpulse.app.data.model.CreditTransaction;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private final List<CreditTransaction> items = new ArrayList<>();

    public void submitList(List<CreditTransaction> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        CreditTransaction item = items.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvName.setText(item.getUserId());
        holder.tvPoints.setText(item.getPoints() + " pts");
        holder.tvType.setText(item.getType());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvPoints, tvType;
        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvLeaderboardName);
            tvPoints = itemView.findViewById(R.id.tvLeaderboardPoints);
            tvType = itemView.findViewById(R.id.tvLeaderboardType);
        }
    }
}
