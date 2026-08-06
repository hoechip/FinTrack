package com.example.fintrack.adapter;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fintrack.R;
import com.example.fintrack.model.PhongBi;
import com.example.fintrack.util.DinhDangTien;

import java.util.ArrayList;

public class PhongBiPhanBoAdapter extends ArrayAdapter<PhongBi> {

    Activity context;
    int resource;
    ArrayList<PhongBi> listPhongBi;
    OnThayDoiSoTienListener listener;

    public interface OnThayDoiSoTienListener {
        void khiThayDoiSoTien();
    }

    public PhongBiPhanBoAdapter(Activity context, int resource, ArrayList<PhongBi> listPhongBi, OnThayDoiSoTienListener listener) {
        super(context, resource, listPhongBi);
        this.context = context;
        this.resource = resource;
        this.listPhongBi = listPhongBi;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return this.listPhongBi != null ? this.listPhongBi.size() : 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View customView = layoutInflater.inflate(resource, null);

        ImageView imgCategoryIcon = customView.findViewById(R.id.img_category_icon);
        TextView tvCategoryName = customView.findViewById(R.id.tv_category_name);
        EditText etAllocatedAmount = customView.findViewById(R.id.et_allocated_amount);

        PhongBi phongBi = listPhongBi.get(position);

        if (phongBi.getTenHangMuc() != null) {
            tvCategoryName.setText(phongBi.getTenHangMuc());
        } else {
            tvCategoryName.setText("");
        }

        ganIconChoHangMuc(imgCategoryIcon, phongBi.getLoaiIcon());

        etAllocatedAmount.setText(DinhDangTien.dinhDangVND(phongBi.getHanMuc()));

        etAllocatedAmount.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                long parsed = DinhDangTien.chuyenChuoiSangSo(s.toString());
                phongBi.setHanMuc(parsed);

                isFormatting = true;
                String formatted = DinhDangTien.dinhDangVND(parsed);
                etAllocatedAmount.setText(formatted);
                etAllocatedAmount.setSelection(Math.max(0, formatted.length() - 2));
                isFormatting = false;

                if (listener != null) {
                    listener.khiThayDoiSoTien();
                }
            }
        });

        return customView;
    }

    private void ganIconChoHangMuc(ImageView imageView, String loaiIcon) {
        if (loaiIcon == null) {
            imageView.setImageResource(R.drawable.ic_wallet);
            return;
        }
        switch (loaiIcon) {
            case "food":
                imageView.setImageResource(R.drawable.ic_food);
                break;
            case "house":
                imageView.setImageResource(R.drawable.ic_home);
                break;
            case "bus":
                imageView.setImageResource(R.drawable.ic_car);
                break;
            case "lightning":
                imageView.setImageResource(R.drawable.ic_bulb);
                break;
            case "gamepad":
                imageView.setImageResource(R.drawable.ic_gamepad);
                break;
            case "shopping":
                imageView.setImageResource(R.drawable.ic_shopping);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_wallet);
                break;
        }
    }

    public ArrayList<PhongBi> getListPhongBi() {
        return listPhongBi;
    }

    public void setListPhongBi(ArrayList<PhongBi> danhSachMoi) {
        this.listPhongBi.clear();
        if (danhSachMoi != null) {
            this.listPhongBi.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }
}
