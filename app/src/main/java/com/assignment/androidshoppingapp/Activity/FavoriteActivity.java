package com.assignment.androidshoppingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.assignment.androidshoppingapp.Adapter.FavoriteAdapter;
import com.assignment.androidshoppingapp.Helper.ManagementFavorites;
import com.assignment.androidshoppingapp.databinding.ActivityFavoriteBinding;
import com.assignment.androidshoppingapp.R;


public class FavoriteActivity extends AppCompatActivity {
    private ActivityFavoriteBinding binding;
    private FavoriteAdapter adapter;
    private ManagementFavorites managementFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managementFavorites = new ManagementFavorites(this);

        setupFavoriteList();
        setListeners();
        setupBottomNavigation();
        setupSearch();

    }

    private void setupFavoriteList() {
        if (managementFavorites.getListFavorites().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView3.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView3.setVisibility(View.VISIBLE);
        }

        adapter = new FavoriteAdapter(managementFavorites.getListFavorites(), this);
        adapter.setOnFavoriteListEmptyListener(() -> {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView3.setVisibility(View.GONE);
        });
        binding.favoriteView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.favoriteView.setAdapter(adapter);
    }


    private void setupBottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.favorites, true);
        binding.bottomNavigation.setOnItemSelectedListener(i -> {
            if (i == R.id.home) {
                startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (i == R.id.cart) {
                startActivity(new Intent(FavoriteActivity.this, CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        binding.editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    if (adapter.getItemCount() == 0) {
                        binding.emptyTxt.setVisibility(View.VISIBLE);
                        binding.scrollView3.setVisibility(View.GONE);
                    } else {
                        binding.emptyTxt.setVisibility(View.GONE);
                        binding.scrollView3.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }
}

