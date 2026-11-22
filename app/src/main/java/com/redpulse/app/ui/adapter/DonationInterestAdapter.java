package com.redpulse.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redpulse.app.R;
import com.redpulse.app.data.model.Donation;
import com.redpulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DonationInterestAdapter extends RecyclerView.Adapter<DonationInterestAdapter.InterestViewHolder> {
    public interface InterestActionListener {
        void onConfirmDonation(Donation donation);
        void onCancelDonation(Donation donation);
    }

    private final List<Donation> items = new ArrayList<>();
    private final InterestActionListener listener;

    public DonationInterestAdapter(InterestActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Donation> donations) {
        items.clear();
        if (donations != null) items.addAll(donations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation_interest, parent, false);
        return new InterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestViewHolder holder, int position) {
        Donation donation = items.get(position);
        holder.tvName.setText(donation.getDonorName());
        String phone = safe(donation.getDonorPhone());
        String email = safe(donation.getDonorEmail());
        holder.tvContact.setText(!phone.isEmpty() || !email.isEmpty()
                ? phone + (email.isEmpty() ? "" : (phone.isEmpty() ? email : " • " + email))
                : "No contact details shared yet");
        holder.tvStatus.setText(Constants.statusLabel(donation.getStatus()));
        holder.tvNote.setText(safe(donation.getNote()).isEmpty() ? "No note added" : donation.getNote());
        boolean canManage = Constants.STATUS_INTEREST_SUBMITTED.equals(donation.getStatus());
        holder.btnConfirm.setVisibility(canManage ? View.VISIBLE : View.GONE);
        holder.btnCancel.setVisibility(canManage ? View.VISIBLE : View.GONE);
        holder.btnConfirm.setOnClickListener(v -> listener.onConfirmDonation(donation));
        holder.btnCancel.setOnClickListener(v -> listener.onCancelDonation(donation));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    static class InterestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContact, tvStatus, tvNote;
        Button btnConfirm, btnCancel;

        InterestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvInterestDonorName);
            tvContact = itemView.findViewById(R.id.tvInterestContact);
            tvStatus = itemView.findViewById(R.id.tvInterestStatus);
            tvNote = itemView.findViewById(R.id.tvInterestNote);
            btnConfirm = itemView.findViewById(R.id.btnConfirmDonation);
            btnCancel = itemView.findViewById(R.id.btnCancelDonation);
        }
    }
}
