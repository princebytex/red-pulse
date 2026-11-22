package com.redpulse.app.ui.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redpulse.app.R;
import com.redpulse.app.data.model.AdminNotice;

import java.util.ArrayList;
import java.util.List;

public class AdminNoticeAdapter extends RecyclerView.Adapter<AdminNoticeAdapter.NoticeViewHolder> {
    public interface NoticeActionListener {
        void onEdit(AdminNotice notice);
        void onDelete(AdminNotice notice);
    }

    private final List<AdminNotice> items = new ArrayList<>();
    private final NoticeActionListener listener;

    public AdminNoticeAdapter(NoticeActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AdminNotice> notices) {
        items.clear();
        if (notices != null) items.addAll(notices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        AdminNotice item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvMessage.setText(item.getMessage());
        String timeLabel = item.getCreatedAt() != null
                ? DateUtils.getRelativeTimeSpanString(item.getCreatedAt().toDate().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
                : "recently";
        holder.tvMeta.setText((item.isActive() ? "Active" : "Hidden") + " • " + timeLabel);
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvMeta;
        Button btnEdit, btnDelete;

        NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminNoticeTitle);
            tvMessage = itemView.findViewById(R.id.tvAdminNoticeMessage);
            tvMeta = itemView.findViewById(R.id.tvAdminNoticeMeta);
            btnEdit = itemView.findViewById(R.id.btnEditNotice);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotice);
        }
    }
}
