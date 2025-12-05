//dialog: màn hình lọc phòng trọ
// Mục đích file: File này dùng để hiển thị dialog lọc phòng trọ theo các tiêu chí
// function: 
// - onCreateDialog(): Tạo dialog và setup các component
// - initViews(): Khởi tạo các view components
// - setupClickListeners(): Thiết lập các sự kiện click
// - onApplyFilterClick(): Xử lý click áp dụng bộ lọc
// - onClearFilterClick(): Xử lý click xóa bộ lọc
// - getFilterData(): Lấy dữ liệu bộ lọc
// - setFilterData(): Thiết lập dữ liệu bộ lọc
package com.example.appquanlytimtro.rooms;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.appquanlytimtro.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class RoomFilterDialog extends DialogFragment {

	public interface OnApplyFilters { void onApply(Map<String, String> filters); }

	private final OnApplyFilters onApplyFilters;

	public RoomFilterDialog(OnApplyFilters onApplyFilters) {
		this.onApplyFilters = onApplyFilters;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_room_filters, null);

		TextInputEditText etCity = v.findViewById(R.id.etCity);
		TextInputEditText etDistrict = v.findViewById(R.id.etDistrict);
		TextInputEditText etMinPrice = v.findViewById(R.id.etMinPrice);
		TextInputEditText etMaxPrice = v.findViewById(R.id.etMaxPrice);
		TextInputEditText etMinArea = v.findViewById(R.id.etMinArea);
		TextInputEditText etMaxArea = v.findViewById(R.id.etMaxArea);
		MaterialButton btnApply = v.findViewById(R.id.btnApply);

		builder.setView(v);
		Dialog d = builder.create();

		btnApply.setOnClickListener(view -> {
			Map<String, String> filters = new HashMap<>();
			putIfNotEmpty(filters, "city", etCity.getText());
			putIfNotEmpty(filters, "district", etDistrict.getText());
			putIfNotEmpty(filters, "minPrice", etMinPrice.getText());
			putIfNotEmpty(filters, "maxPrice", etMaxPrice.getText());
			putIfNotEmpty(filters, "minArea", etMinArea.getText());
			putIfNotEmpty(filters, "maxArea", etMaxArea.getText());
			filters.put("status", "active");
			if (onApplyFilters != null) onApplyFilters.onApply(filters);
			d.dismiss();
		});

		return d;
	}

	private void putIfNotEmpty(Map<String, String> map, String key, CharSequence val) {
		if (val != null) {
			String s = val.toString().trim();
			if (!s.isEmpty()) map.put(key, s);
		}
	}
}
