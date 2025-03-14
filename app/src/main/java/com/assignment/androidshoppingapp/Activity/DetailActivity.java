package com.assignment.androidshoppingapp.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.assignment.androidshoppingapp.Domain.ItemsModel;
import com.assignment.androidshoppingapp.Helper.ManagementFavorites;
import com.assignment.androidshoppingapp.R;
import com.assignment.androidshoppingapp.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private ItemsModel object;
    private ManagementFavorites managementFavorites;
    private boolean isFavorite = false; // Track favorite state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managementFavorites = new ManagementFavorites(this);

        getBundles();
        checkIfFavorite();
        setupFavoriteButton();
    }

    private void getBundles() {
        object = (ItemsModel) getIntent().getSerializableExtra("object");
        binding.titleTxt.setText(object.getTitle());
        binding.priceTxt.setText("$" + object.getPrice());

        // Initialize favorite button state
        checkIfFavorite();
    }

    private void checkIfFavorite() {
        isFavorite = managementFavorites.isFavorite(object);
        updateFavButton();
    }

    private void setupFavoriteButton() {
        binding.favBtn.setOnClickListener(v -> {
            if (isFavorite) {
                managementFavorites.removeFavorite(object);
                isFavorite = false;
            } else {
                managementFavorites.addFavorite(object);
                isFavorite = true;
            }
            updateFavButton();
        });
    }

    private void updateFavButton() {
        if (isFavorite) {
            binding.favBtn.setImageResource(R.drawable.fav_selected); // Change to "filled heart"
        } else {
            binding.favBtn.setImageResource(R.drawable.fav); // Change to "empty heart"
        }
    }
}
