package com.assignment.androidshoppingapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.assignment.androidshoppingapp.R;
import com.assignment.androidshoppingapp.databinding.ViewholderColorBinding;

import java.util.ArrayList;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.Viewholder> {
    ArrayList<String> items;
    Context context;
    int selectedPosition = -1;
    int lastSelectedPosition = -1;

    public ColorAdapter(ArrayList<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderColorBinding binding = ViewholderColorBinding.inflate(LayoutInflater.from(context), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);
            }
        });

        if(selectedPosition==holder.getAdapterPosition()){
            Drawable unwrappedDrawable= AppCompatResources.getDrawable(context, R.drawable.color_selected);
            Glide.with(context)
                    .load(unwrappedDrawable)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.binding.colorLayout)   ;
        }else{
            Drawable unwrappedDrawable= AppCompatResources.getDrawable(context, R.drawable.color_selected);
            Drawable wrappedDrawable= DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(items.get(position)));

            Glide.with(context)
                    .load(wrappedDrawable)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.binding.colorLayout)   ;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderColorBinding binding;

        public Viewholder(ViewholderColorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
