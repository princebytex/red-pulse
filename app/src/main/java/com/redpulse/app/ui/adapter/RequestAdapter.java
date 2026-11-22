package com.redpulse.app.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redpulse.app.R;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    public enum Mode {
        HOME,
        FEED,
        MY_REQUESTS,
        ADMIN_MANAGE
    }

    public interface RequestActionListener {
        void onPrimaryAction(BloodRequest request);
        void onSecondaryAction(BloodRequest request);
        void onCardClicked(BloodRequest request);
    }

    private final List<BloodRequest> items = new ArrayList<>();
    private final Mode mode;
    private final RequestActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public RequestAdapter(Mode mode, RequestActionListener listener) {
        this.mode = mode;
        this.listener = listener;
    }

    public void submitList(List<BloodRequest> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BloodRequest item = items.get(position);
        holder.tvPatientName.setText(item.getPatientName());
        holder.tvMeta.setText(item.getBloodGroup() + " • " + item.getRequiredUnits() + " unit(s) • " +
                (item.isEmergency() ? "Emergency" : "Standard") + " • " + item.getMatchedDonorCount() + " donor(s)");
        holder.tvHospital.setText(item.getHospitalName());
        holder.tvAddress.setText(item.getFullAddress());
        holder.tvDueDate.setText(item.getRequiredTimestamp() != null
                ? "Needed by " + dateFormat.format(new Date(item.getRequiredTimestamp().toDate().getTime()))
                : "Time not specified");
        holder.tvStatus.setText(Constants.statusLabel(item.getStatus()));
        holder.tvReason.setVisibility(View.GONE);
        holder.btnPrimary.setVisibility(View.GONE);
        holder.btnSecondary.setVisibility(View.GONE);

        String reason = firstNonEmpty(item.getAdminDecisionReason(), item.getCancellationReason(), item.getCompletionNote(), item.getNotes());
        if (!TextUtils.isEmpty(reason) && mode != Mode.HOME) {
            holder.tvReason.setText(reason);
            holder.tvReason.setVisibility(View.VISIBLE);
        }

        if (mode == Mode.FEED) {
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnPrimary.setText("I'm interested");
            holder.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(item));
        } else if (mode == Mode.MY_REQUESTS) {
            if (Constants.STATUS_PUBLISHED.equals(item.getStatus()) && item.isActive()) {
                holder.btnPrimary.setVisibility(View.VISIBLE);
                holder.btnPrimary.setText("Interested donors");
                holder.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(item));

                holder.btnSecondary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setText("Cancel request");
                holder.btnSecondary.setOnClickListener(v -> listener.onSecondaryAction(item));
            } else if (Constants.STATUS_PENDING_REVIEW.equals(item.getStatus())) {
                holder.btnSecondary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setText("Cancel request");
                holder.btnSecondary.setOnClickListener(v -> listener.onSecondaryAction(item));
            }
        } else if (mode == Mode.ADMIN_MANAGE) {
            if (Constants.STATUS_PENDING_REVIEW.equals(item.getStatus())) {
                holder.btnPrimary.setVisibility(View.VISIBLE);
                holder.btnPrimary.setText("Approve");
                holder.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(item));
                holder.btnSecondary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setText("Reject");
                holder.btnSecondary.setOnClickListener(v -> listener.onSecondaryAction(item));
            } else if (Constants.STATUS_PUBLISHED.equals(item.getStatus()) && item.isActive()) {
                holder.btnPrimary.setVisibility(View.VISIBLE);
                holder.btnPrimary.setText("Edit");
                holder.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(item));
                holder.btnSecondary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setText("Delete");
                holder.btnSecondary.setOnClickListener(v -> listener.onSecondaryAction(item));
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onCardClicked(item));
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvMeta, tvHospital, tvAddress, tvDueDate, tvStatus, tvReason;
        Button btnPrimary, btnSecondary;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvMeta = itemView.findViewById(R.id.tvRequestMeta);
            tvHospital = itemView.findViewById(R.id.tvHospitalName);
            tvAddress = itemView.findViewById(R.id.tvRequestAddress);
            tvDueDate = itemView.findViewById(R.id.tvRequestDueDate);
            tvStatus = itemView.findViewById(R.id.tvRequestStatus);
            tvReason = itemView.findViewById(R.id.tvRequestReason);
            btnPrimary = itemView.findViewById(R.id.btnRequestPrimary);
            btnSecondary = itemView.findViewById(R.id.btnRequestSecondary);
        }
    }
}
