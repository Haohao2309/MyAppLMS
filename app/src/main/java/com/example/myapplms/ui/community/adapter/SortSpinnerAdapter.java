package com.example.myapplms.ui.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.myapplms.R;

public class SortSpinnerAdapter extends ArrayAdapter<String> {

    private final String[] options;
    private final int[] icons = {R.drawable.ic_time, R.drawable.ic_trending, R.drawable.ic_message};
    private int selectedPosition = 0;

    public SortSpinnerAdapter(@NonNull Context context, String[] options) {
        super(context, R.layout.item_spinner_sort_selected, options);
        this.options = options;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_sort_selected, parent, false);
        }

        ImageView ivIcon = convertView.findViewById(R.id.ivSortIcon);
        TextView tvName = convertView.findViewById(R.id.tvSortName);

        ivIcon.setImageResource(icons[position]);
        tvName.setText(options[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_sort_dropdown, parent, false);
        }

        ImageView ivIcon = convertView.findViewById(R.id.ivSortIcon);
        TextView tvName = convertView.findViewById(R.id.tvSortName);
        ImageView ivCheck = convertView.findViewById(R.id.ivCheckmark);

        ivIcon.setImageResource(icons[position]);
        tvName.setText(options[position]);

        if (position == selectedPosition) {
            ivCheck.setVisibility(View.VISIBLE);
            tvName.setTextColor(ContextCompat.getColor(getContext(), R.color.primary));
            ivIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary));
        } else {
            ivCheck.setVisibility(View.GONE);
            tvName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            ivIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_primary));
        }

        return convertView;
    }
}
