package com.assignment.androidshoppingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.androidshoppingapp.Domain.CategoryModel;
import com.assignment.androidshoppingapp.R;
import com.assignment.androidshoppingapp.databinding.ViewholderCategoryBinding;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<CategoryModel> items;
    private Context context;
    private int selectedPosition = 0;
    private int lastSelectedPosition = -1;
    private OnCategoryClickListener onCategoryClickListener; // Thêm listener

    // Định nghĩa interface cho callback
    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryTitle);
    }

    // Constructor cập nhật để nhận listener
    public CategoryAdapter(ArrayList<CategoryModel> items, OnCategoryClickListener listener) {
        this.items = items;
        this.onCategoryClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.titleTxt.setText(items.get(position).getTitle());

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastSelectedPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);

                // Gọi callback để thông báo danh mục được chọn
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryClick(items.get(position).getTitle());
                }
            }
        });

        if (selectedPosition == position) {
            holder.binding.titleTxt.setBackgroundResource(R.drawable.orange_bg);
            holder.binding.titleTxt.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.binding.titleTxt.setBackgroundResource(R.drawable.stroke_bg);
            holder.binding.titleTxt.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCategoryBinding binding;

        public ViewHolder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}