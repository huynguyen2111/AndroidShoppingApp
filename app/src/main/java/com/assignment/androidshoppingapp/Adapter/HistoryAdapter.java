package com.assignment.androidshoppingapp.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.assignment.androidshoppingapp.databinding.ViewholderHistoryBinding;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private ArrayList<String> historyList;

    public HistoryAdapter(ArrayList<String> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderHistoryBinding binding = ViewholderHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String historyEntry = historyList.get(position);
        // Tách chuỗi thành thời gian và nội dung
        String[] parts = historyEntry.split(" - ", 2); // Tách tại " - "
        if (parts.length == 2) {
            holder.binding.timestampText.setText(parts[0]); // Thời gian
            holder.binding.purchasedItemsText.setText(parts[1]); // Nội dung
        } else {
            holder.binding.timestampText.setText(historyEntry); // Nếu không tách được, hiển thị toàn bộ
            holder.binding.purchasedItemsText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderHistoryBinding binding;

        public ViewHolder(ViewholderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}