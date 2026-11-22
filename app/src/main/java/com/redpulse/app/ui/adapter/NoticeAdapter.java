package com.redpulse.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.redpulse.app.R;
import com.redpulse.app.data.model.AdminNotice;
import java.util.ArrayList;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {
    private final List<AdminNotice> items = new ArrayList<>();

    public void submitList(List<AdminNotice> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        AdminNotice item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvMessage.setText(item.getMessage());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;
        NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvMessage = itemView.findViewById(R.id.tvNoticeMessage);
        }
    }
}
