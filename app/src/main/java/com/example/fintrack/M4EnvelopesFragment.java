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
import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;
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
    private FirebaseHelper firebaseHelper;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        firebaseHelper = FirebaseHelper.layThucThe();
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
        firebaseHelper.langNgheKeHoachNganSach(new FirebaseHelper.LangNgheDuLieu() {
            @Override
            public void onDuLieu(KeHoachNganSach keHoach) {
                if (keHoach != null && keHoach.getDanhSachPhongBi() != null) {
                    envelopeList.clear();
                    for (PhongBi pb : keHoach.getDanhSachPhongBi()) {
                        envelopeList.add(chuyenPhongBiSangEnvelope(pb));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String loi) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + loi, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Envelope chuyenPhongBiSangEnvelope(PhongBi pb) {
        Envelope envelope = new Envelope(pb.getId(), pb.getTenHangMuc(), (int) pb.getDaTieu(), (int) pb.getHanMuc(), pb.getLoaiIcon(), null, 0);
        
        // Map loaiIcon string to drawable resource
        int iconRes = R.drawable.ic_wallet;
        if (pb.getLoaiIcon() != null) {
            switch (pb.getLoaiIcon()) {
                case "food": iconRes = R.drawable.ic_food; break;
                case "house": iconRes = R.drawable.ic_home; break;
                case "bus": iconRes = R.drawable.ic_car; break;
                case "lightning": iconRes = R.drawable.ic_bulb; break;
                case "gamepad": iconRes = R.drawable.ic_gamepad; break;
                case "shopping": iconRes = R.drawable.ic_shopping; break;
            }
        }
        envelope.setIconRes(iconRes);
        return envelope;
    }

    private String selectedIconTag = "food";

    private void showAddEditDialog(Envelope envelope, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        boolean isEdit = envelope != null;
        builder.setTitle(isEdit ? "Sửa phong bì" : "Thêm phong bì mới");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_envelope, null);
        final EditText input = dialogView.findViewById(R.id.edit_envelope_name);
        final androidx.recyclerview.widget.RecyclerView rvIcons = dialogView.findViewById(R.id.rv_icon_selection);

        if (isEdit) {
            input.setText(envelope.getName());
            input.setSelection(input.getText().length());
            selectedIconTag = envelope.getIconName() != null ? envelope.getIconName() : "food";
        } else {
            selectedIconTag = "food";
        }

        // Danh sách icon phong phú và không bị lặp lại
        java.util.List<String> iconTags = java.util.Arrays.asList(
                "food", "home", "car", "bulb", "gamepad", "shopping", "wallet",
                "movie", "person", "calendar_today", "history", "settings"
        );

        IconSelectionAdapter iconAdapter = new IconSelectionAdapter(iconTags, selectedIconTag, tag -> {
            selectedIconTag = tag;
        });
        
        rvIcons.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), 
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        rvIcons.setAdapter(iconAdapter);
        
        builder.setView(dialogView);

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Thêm", (dialog, which) -> {
            String name = input.getText().toString().trim();
            
            if (!name.isEmpty()) {
                KeHoachNganSach keHoach = firebaseHelper.layKeHoachHienTai();
                if (keHoach == null) return;
                
                ArrayList<PhongBi> danhSach = keHoach.getDanhSachPhongBi();
                if (isEdit) {
                    for (PhongBi pb : danhSach) {
                        if (pb.getId().equals(envelope.getId())) {
                            pb.setTenHangMuc(name);
                            pb.setLoaiIcon(selectedIconTag);
                            break;
                        }
                    }
                } else {
                    String id = String.valueOf(System.currentTimeMillis());
                    danhSach.add(new PhongBi(id, name, selectedIconTag, 0L, 0L));
                }
                
                firebaseHelper.luuKeHoachNganSach(keHoach, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), isEdit ? "Đã cập nhật" : "Đã thêm phong bì", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    KeHoachNganSach keHoach = firebaseHelper.layKeHoachHienTai();
                    if (keHoach != null && keHoach.getDanhSachPhongBi() != null) {
                        ArrayList<PhongBi> danhSach = keHoach.getDanhSachPhongBi();
                        for (int i = 0; i < danhSach.size(); i++) {
                            if (danhSach.get(i).getId().equals(envelope.getId())) {
                                danhSach.remove(i);
                                break;
                            }
                        }
                        firebaseHelper.luuKeHoachNganSach(keHoach, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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
