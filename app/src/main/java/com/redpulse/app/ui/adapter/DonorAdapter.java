package com.redpulse.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redpulse.app.R;
import com.redpulse.app.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {
    private final List<User> items = new ArrayList<>();

    public void submitList(List<User> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donor, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        User item = items.get(position);
        holder.tvName.setText(item.getFullName());
        holder.tvGroup.setText(item.getBloodGroup());
        holder.tvAddress.setText(item.getAddress());
        holder.tvDistance.setText(item.getDistanceKm() != null
                ? String.format(Locale.getDefault(), "%.1f km away", item.getDistanceKm())
                : "Distance unavailable");
        holder.tvAvailability.setText(item.isAvailable() ? "Available now" : "Unavailable");
        String phone = item.getMobileNumber() == null ? "" : item.getMobileNumber().trim();
        String email = item.getEmail() == null ? "" : item.getEmail().trim();
        holder.tvContact.setText(!phone.isEmpty() || !email.isEmpty()
                ? phone + (email.isEmpty() ? "" : (phone.isEmpty() ? email : " • " + email))
                : "Contact details available after profile update");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DonorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGroup, tvAddress, tvDistance, tvAvailability, tvContact;

        DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDonorName);
            tvGroup = itemView.findViewById(R.id.tvDonorBloodGroup);
            tvAddress = itemView.findViewById(R.id.tvDonorAddress);
            tvDistance = itemView.findViewById(R.id.tvDonorDistance);
            tvAvailability = itemView.findViewById(R.id.tvDonorAvailability);
            tvContact = itemView.findViewById(R.id.tvDonorContact);
        }
    }
}
