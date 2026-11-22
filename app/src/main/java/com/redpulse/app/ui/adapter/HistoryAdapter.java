package com.redpulse.app.ui.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redpulse.app.R;
import com.redpulse.app.data.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final List<HistoryItem> items = new ArrayList<>();

    public void submitList(List<HistoryItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.tvBadge.setText(item.getBadge());
        holder.tvTime.setText(item.getSortTime() > 0
                ? DateUtils.getRelativeTimeSpanString(item.getSortTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
                : "Recently");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        TextView tvBadge;
        TextView tvTime;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvSubtitle = itemView.findViewById(R.id.tvHistorySubtitle);
            tvBadge = itemView.findViewById(R.id.tvHistoryBadge);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
        }
    }
}
