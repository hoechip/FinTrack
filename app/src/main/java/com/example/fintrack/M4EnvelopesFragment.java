package com.example.fintrack;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fintrack.databinding.FragmentSecondBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class M4EnvelopesFragment extends Fragment {

    private FragmentSecondBinding binding;
    private List<Envelope> envelopeList;
    private EnvelopeAdapter adapter;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference("envelopes");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        envelopeList = new ArrayList<>();
        binding.rvM4Envelopes.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new EnvelopeAdapter(envelopeList, new EnvelopeAdapter.OnEnvelopeActionListener() {
            @Override
            public void onEdit(Envelope envelope, int position) {
                showAddEditDialog(envelope, position);
            }

            @Override
            public void onDelete(Envelope envelope, int position) {
                showDeleteConfirmDialog(envelope, position);
            }
        });
        binding.rvM4Envelopes.setAdapter(adapter);

        // Lắng nghe dữ liệu từ Firebase
        listenToFirebase();

        binding.btnM4Back.setOnClickListener(v ->
                NavHostFragment.findNavController(M4EnvelopesFragment.this)
                        .navigate(R.id.action_m4_to_m3)
        );

        binding.btnM4CreateEnvelope.setOnClickListener(v -> showAddEditDialog(null, -1));

        binding.btnM4Settings.setOnClickListener(v -> 
            Toast.makeText(getContext(), R.string.msg_in_development, Toast.LENGTH_SHORT).show()
        );
    }

    private void listenToFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                envelopeList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Envelope envelope = postSnapshot.getValue(Envelope.class);
                    if (envelope != null) {
                        envelopeList.add(envelope);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDialog(Envelope envelope, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        boolean isEdit = envelope != null;
        builder.setTitle(isEdit ? "Sửa phong bì" : "Thêm phong bì mới");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_envelope, null);
        final EditText input = dialogView.findViewById(R.id.edit_envelope_name);

        if (isEdit) {
            input.setText(envelope.getName());
            input.setSelection(input.getText().length());
        }
        builder.setView(dialogView);

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Thêm", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                if (isEdit) {
                    // Cập nhật trên Firebase
                    envelope.setName(name);
                    mDatabase.child(envelope.getId()).setValue(envelope);
                } else {
                    // Thêm mới vào Firebase
                    String id = mDatabase.push().getKey();
                    Envelope newEnv = new Envelope(id, name, R.drawable.ic_food_thin);
                    if (id != null) {
                        mDatabase.child(id).setValue(newEnv);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteConfirmDialog(Envelope envelope, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa phong bì \"" + envelope.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa trên Firebase
                    mDatabase.child(envelope.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
