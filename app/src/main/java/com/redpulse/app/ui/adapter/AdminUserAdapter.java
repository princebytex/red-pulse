package com.redpulse.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redpulse.app.R;
import com.redpulse.app.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder> {
    public interface UserActionListener {
        void onHistory(User user);
        void onCredit(User user);
        void onDelete(User user);
    }

    private final List<User> items = new ArrayList<>();
    private final UserActionListener listener;

    public AdminUserAdapter(UserActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<User> users) {
        items.clear();
        if (users != null) items.addAll(users);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new AdminUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder holder, int position) {
        User user = items.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvMeta.setText(user.getEmail() + " • " + user.getMobileNumber());
        holder.tvStatus.setText((user.isBlocked() ? "Blocked" : "Active") + " • Credits: " + user.getTotalCredits());
        holder.tvAddress.setText(user.getSafeDisplayAddress());
        holder.btnHistory.setOnClickListener(v -> listener.onHistory(user));
        holder.btnCredit.setOnClickListener(v -> listener.onCredit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMeta, tvStatus, tvAddress;
        Button btnHistory, btnCredit, btnDelete;

        AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminUserName);
            tvMeta = itemView.findViewById(R.id.tvAdminUserMeta);
            tvStatus = itemView.findViewById(R.id.tvAdminUserStatus);
            tvAddress = itemView.findViewById(R.id.tvAdminUserAddress);
            btnHistory = itemView.findViewById(R.id.btnUserHistory);
            btnCredit = itemView.findViewById(R.id.btnUserCredit);
            btnDelete = itemView.findViewById(R.id.btnUserDelete);
        }
    }
}
