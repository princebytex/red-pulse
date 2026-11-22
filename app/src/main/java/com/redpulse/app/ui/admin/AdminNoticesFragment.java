package com.redpulse.app.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.redpulse.app.R;
import com.redpulse.app.data.model.AdminNotice;
import com.redpulse.app.data.repository.AdminRepository;
import com.redpulse.app.ui.adapter.AdminNoticeAdapter;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.List;

public class AdminNoticesFragment extends Fragment {
    private final AdminRepository adminRepository = new AdminRepository();
    private String editingNoticeId;
    private Timestamp editingCreatedAt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText etTitle = view.findViewById(R.id.etNoticeTitle);
        EditText etMessage = view.findViewById(R.id.etNoticeMessage);
        SwitchCompat switchActive = view.findViewById(R.id.switchNoticeActive);
        Button btnSave = view.findViewById(R.id.btnPublishNotice);
        Button btnClear = view.findViewById(R.id.btnClearNoticeEditor);
        TextView tvEmpty = view.findViewById(R.id.tvNoticeEmptyState);
        RecyclerView recyclerView = view.findViewById(R.id.rvAdminNotices);

        AdminNoticeAdapter adapter = new AdminNoticeAdapter(new AdminNoticeAdapter.NoticeActionListener() {
            @Override
            public void onEdit(AdminNotice notice) {
                editingNoticeId = notice.getId();
                editingCreatedAt = notice.getCreatedAt();
                etTitle.setText(notice.getTitle());
                etMessage.setText(notice.getMessage());
                switchActive.setChecked(notice.isActive());
                btnSave.setText("Update notice");
                Toast.makeText(requireContext(), "Notice loaded into editor", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(AdminNotice notice) {
                showDeleteDialog(notice);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        MutableLiveData<List<AdminNotice>> noticesLiveData = new MutableLiveData<>();
        adminRepository.observeNotices(noticesLiveData);
        noticesLiveData.observe(getViewLifecycleOwner(), notices -> {
            adapter.submitList(notices);
            tvEmpty.setVisibility(notices == null || notices.isEmpty() ? View.VISIBLE : View.GONE);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(), "Enter both title and message", Toast.LENGTH_SHORT).show();
                return;
            }
            final boolean wasEditing = editingNoticeId != null;
            AdminNotice notice = new AdminNotice();
            notice.setId(editingNoticeId);
            notice.setTitle(title);
            notice.setMessage(message);
            notice.setPriority("normal");
            notice.setActive(switchActive.isChecked());
            notice.setCreatedAt(editingCreatedAt != null ? editingCreatedAt : Timestamp.now());
            adminRepository.saveNotice(notice, FirebaseAuth.getInstance().getUid(), new FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    clearEditor(etTitle, etMessage, switchActive, btnSave);
                    Toast.makeText(requireContext(), wasEditing ? "Notice updated." : "Notice saved.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnClear.setOnClickListener(v -> clearEditor(etTitle, etMessage, switchActive, btnSave));
    }

    private void showDeleteDialog(AdminNotice notice) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete notice")
                .setMessage("This notice will be removed from the system for all users.")
                .setNegativeButton("Back", null)
                .setPositiveButton("Delete", (dialog, which) -> adminRepository.deleteNotice(notice.getId(), new FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(requireContext(), "Notice deleted.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }))
                .show();
    }

    private void clearEditor(EditText etTitle, EditText etMessage, SwitchCompat switchActive, Button btnSave) {
        editingNoticeId = null;
        editingCreatedAt = null;
        etTitle.setText("");
        etMessage.setText("");
        switchActive.setChecked(true);
        btnSave.setText("Save notice");
    }
}
