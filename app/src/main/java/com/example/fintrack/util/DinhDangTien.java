package com.example.fintrack.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DinhDangTien {

    private static final DecimalFormat dinhDangSo;

    static {
        DecimalFormatSymbols kyHieu = new DecimalFormatSymbols(Locale.getDefault());
        kyHieu.setGroupingSeparator(',');
        dinhDangSo = new DecimalFormat("#,###", kyHieu);
    }

    public static String dinhDangVND(long soTien) {
        return dinhDangSo.format(soTien) + " đ";
    }

    public static String dinhDangVNDCoDau(long soTien) {
        if (soTien > 0) {
            return "+" + dinhDangSo.format(soTien) + " đ";
        } else if (soTien < 0) {
            return "-" + dinhDangSo.format(Math.abs(soTien)) + " đ";
        } else {
            return "0 đ";
        }
    }

    public static long chuyenChuoiSangSo(String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            return 0;
        }
        try {
            String chiChuaSo = chuoi.replaceAll("[^0-9]", "");
            if (chiChuaSo.isEmpty()) {
                return 0;
            }
            return Long.parseLong(chiChuaSo);
        } catch (Exception e) {
            return 0;
        }
    }
}
