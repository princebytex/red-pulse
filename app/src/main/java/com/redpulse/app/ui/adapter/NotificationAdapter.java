package com.redpulse.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.redpulse.app.R;
import com.redpulse.app.data.model.NotificationItem;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final List<NotificationItem> items = new ArrayList<>();

    public void submitList(List<NotificationItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvBody.setText(item.getBody());
        holder.tvType.setText(item.getType());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvType;
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvBody = itemView.findViewById(R.id.tvNotificationBody);
            tvType = itemView.findViewById(R.id.tvNotificationType);
        }
    }
}
