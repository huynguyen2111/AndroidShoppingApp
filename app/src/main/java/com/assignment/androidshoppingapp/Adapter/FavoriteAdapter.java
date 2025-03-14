package com.assignment.androidshoppingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.assignment.androidshoppingapp.Domain.ItemsModel;
import com.assignment.androidshoppingapp.Helper.ManagementFavorites;
import com.assignment.androidshoppingapp.R;
import java.util.ArrayList;
import java.util.Locale;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    private ArrayList<ItemsModel> favoriteItems;
    private ArrayList<ItemsModel> originalItems; // for search filtering
    private Context context;
    private ManagementFavorites managementFavorites;
    // Listener to notify FavoriteActivity when list becomes empty
    private OnFavoriteListEmptyListener emptyListener;

    public interface OnFavoriteListEmptyListener {
        void onFavoriteListEmpty();
    }

    public void setOnFavoriteListEmptyListener(OnFavoriteListEmptyListener listener) {
        this.emptyListener = listener;
    }

    public FavoriteAdapter(ArrayList<ItemsModel> favoriteItems, Context context) {
        this.favoriteItems = favoriteItems;
        // Make a copy of the original list for filtering
        this.originalItems = new ArrayList<>(favoriteItems);
        this.context = context;
        this.managementFavorites = new ManagementFavorites(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModel item = favoriteItems.get(position);
        holder.title.setText(item.getTitle());
        holder.price.setText("$" + item.getPrice());
        Glide.with(context).load(item.getPicUrl().get(0)).into(holder.image);

        // Optionally, add an animation when binding the view
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        holder.itemView.startAnimation(animation);

        holder.removeBtn.setOnClickListener(v -> {
            // Remove from the favorites data source
            managementFavorites.removeFavorite(item);
            // Remove from adapter list with animation
            favoriteItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, favoriteItems.size());
            // Update original list too for search filtering
            originalItems.remove(item);
            // If list becomes empty, call listener
            if (favoriteItems.isEmpty() && emptyListener != null) {
                emptyListener.onFavoriteListEmpty();
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteItems.size();
    }

    public void filter(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
        favoriteItems.clear();
        if (lowerCaseQuery.isEmpty()) {
            favoriteItems.addAll(originalItems);
        } else {
            for (ItemsModel item : originalItems) {
                if (item.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    favoriteItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView image, removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Make sure these IDs match your XML layout
            title = itemView.findViewById(R.id.titleTxt);
            price = itemView.findViewById(R.id.feeEachItem);
            image = itemView.findViewById(R.id.pic);
            removeBtn = itemView.findViewById(R.id.removeBtn);
        }
    }
}
