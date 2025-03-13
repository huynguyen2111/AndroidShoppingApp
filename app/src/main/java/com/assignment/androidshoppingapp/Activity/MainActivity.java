package com.assignment.androidshoppingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.assignment.androidshoppingapp.Adapter.CategoryAdapter;
import com.assignment.androidshoppingapp.Adapter.PopularAdapter;
import com.assignment.androidshoppingapp.Adapter.SliderAdapter;
import com.assignment.androidshoppingapp.Domain.BannerModel;
import com.assignment.androidshoppingapp.R;
import com.assignment.androidshoppingapp.ViewModel.MainViewModel;
import com.assignment.androidshoppingapp.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);
        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {

            }
        });

        binding.cartBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.loadPopular().observeForever(itemsModels -> {
            if (!itemsModels.isEmpty()) {
                binding.popularView.setLayoutManager(
                        new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                binding.popularView.setNestedScrollingEnabled(true);
            }
            binding.progressBarPopular.setVisibility(View.GONE);
        });
        viewModel.loadPopular();
    }

    private void initSlider() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        viewModel.loadBanner().observeForever(bannerModels -> {
            if (bannerModels != null && !bannerModels.isEmpty()) {
                banners(bannerModels);
                binding.progressBarSlider.setVisibility(View.GONE);
            }
        });

        viewModel.loadBanner();
    }

    private void banners(ArrayList<BannerModel> bannerModels) {
        binding.viewPagerSlider.setAdapter(new SliderAdapter(bannerModels, binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        viewModel.loadCategory().observeForever(categoryModels -> {
            binding.categoryView.setLayoutManager(new LinearLayoutManager(
                    MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
            binding.categoryView.setAdapter(new CategoryAdapter(categoryModels));
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }
}