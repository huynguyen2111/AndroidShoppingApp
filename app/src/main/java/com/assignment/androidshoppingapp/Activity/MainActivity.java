package com.assignment.androidshoppingapp.Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


import com.assignment.androidshoppingapp.Adapter.CategoryAdapter;
import com.assignment.androidshoppingapp.Adapter.HistoryAdapter;
import com.assignment.androidshoppingapp.Adapter.PopularAdapter;
import com.assignment.androidshoppingapp.Adapter.SliderAdapter;
import com.assignment.androidshoppingapp.Domain.BannerModel;
import com.assignment.androidshoppingapp.R;
import com.assignment.androidshoppingapp.ViewModel.MainViewModel;
import com.assignment.androidshoppingapp.databinding.ActivityMainBinding;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private boolean hasNotification = false;
    private boolean isHistoryVisible = false;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "CheckoutHistory";
    private static final String KEY_HISTORY = "checkout_history";
    private HistoryAdapter historyAdapter;
    private ArrayList<String> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Khởi tạo danh sách lịch sử và sắp xếp
        historyList = new ArrayList<>(sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>()));
        sortHistoryList(); // Sắp xếp danh sách theo thời gian giảm dần

        historyAdapter = new HistoryAdapter(historyList);
        binding.historyView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyView.setAdapter(historyAdapter);

        // Thêm chức năng vuốt để xóa
        setupSwipeToDelete();

        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
        handleCheckoutResult();
        setupBellIcon();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // Không hỗ trợ kéo thả
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String itemToDelete = historyList.get(position);

                // Xóa mục khỏi danh sách
                historyList.remove(position);
                historyAdapter.notifyItemRemoved(position);

                // Cập nhật SharedPreferences
                updateSharedPreferences();

                // Nếu danh sách trống, ẩn historyView
                if (historyList.isEmpty()) {
                    binding.historyView.setVisibility(View.GONE);
                    isHistoryVisible = false;
                }

                Toast.makeText(MainActivity.this, "Đã xóa: " + itemToDelete, Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.historyView);
    }

    private void updateSharedPreferences() {
        // Cập nhật SharedPreferences với danh sách mới
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_HISTORY, new HashSet<>(historyList));
        editor.apply();
    }

    private void handleCheckoutResult() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("checkout_success", false)) {
            String purchasedItems = intent.getStringExtra("purchased_items");
            saveCheckoutHistory(purchasedItems);
            hasNotification = true;
            updateBellIcon();
        }
    }

    private void saveCheckoutHistory(String purchasedItems) {
        // Thêm timestamp vào lịch sử
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String historyEntry = timestamp + " - " + purchasedItems;

        // Lấy danh sách lịch sử hiện tại
        Set<String> historySet = sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>());
        Set<String> updatedHistory = new HashSet<>(historySet);

        // Kiểm tra trùng lặp trước khi thêm
        if (!updatedHistory.contains(historyEntry)) {
            updatedHistory.add(historyEntry);
        }

        // Lưu lại danh sách mới
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_HISTORY, updatedHistory);
        editor.apply();

        // Cập nhật danh sách hiển thị
        historyList.clear();
        historyList.addAll(updatedHistory);
        sortHistoryList(); // Sắp xếp lại sau khi thêm
        historyAdapter.notifyDataSetChanged();
    }

    private void sortHistoryList() {
        // Sắp xếp theo thời gian giảm dần (mới nhất ở trên cùng)
        Collections.sort(historyList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    Date date1 = sdf.parse(o1.split(" - ")[0]);
                    Date date2 = sdf.parse(o2.split(" - ")[0]);
                    return date2.compareTo(date1); // Giảm dần
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    private void updateBellIcon() {
        if (hasNotification) {
            binding.imageView3.setImageResource(R.drawable.bell_icon_with_notification);
        } else {
            binding.imageView3.setImageResource(R.drawable.bell_icon);
        }
    }

    private void setupBellIcon() {
        binding.imageView3.setOnClickListener(v -> {
            // Toggle hiển thị/ẩn lịch sử
            isHistoryVisible = !isHistoryVisible;
            if (isHistoryVisible) {
                if (historyList.isEmpty()) {
                    Toast.makeText(this, "Chưa có lịch sử thanh toán", Toast.LENGTH_SHORT).show();
                    isHistoryVisible = false;
                } else {
                    binding.historyView.setVisibility(View.VISIBLE);
                    binding.historyView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                    hasNotification = false; // Xóa ký hiệu thông báo sau khi xem
                    updateBellIcon();
                }
            } else {
                Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.historyView.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                binding.historyView.startAnimation(slideOut);
            }
        });
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);
        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.favorites) {
                    startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                } else if (i == R.id.cart) {
                    startActivity(new Intent(MainActivity.this, CartActivity.class));
                }
                // Xử lý các tab khác nếu cần
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