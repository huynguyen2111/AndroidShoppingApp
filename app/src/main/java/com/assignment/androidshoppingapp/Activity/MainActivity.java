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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.assignment.androidshoppingapp.Domain.ItemsModel;

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
    private PopularAdapter popularAdapter;
    private ArrayList<ItemsModel> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        historyList = new ArrayList<>(sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>()));
        sortHistoryList();

        historyAdapter = new HistoryAdapter(historyList);
        binding.historyView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyView.setAdapter(historyAdapter);

        setupSwipeToDelete();

        Intent intent = getIntent();
        String userName = intent.getStringExtra("name");
        if (userName != null && !userName.isEmpty()) {
            binding.textView5.setText(userName);
        } else {
            binding.textView5.setText("Guest");
        }

        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
        handleCheckoutResult();
        setupBellIcon();

        binding.imageView2.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            profileIntent.putExtras(getIntent().getExtras());
            startActivity(profileIntent);
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String itemToDelete = historyList.get(position);
                historyList.remove(position);
                historyAdapter.notifyItemRemoved(position);
                updateSharedPreferences();
                if (historyList.isEmpty()) {
                    binding.historyContainer.setVisibility(View.GONE); // Sử dụng historyContainer
                    isHistoryVisible = false;
                }
                Toast.makeText(MainActivity.this, "Đã xóa: " + itemToDelete, Toast.LENGTH_SHORT).show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.historyView);
    }

    private void updateSharedPreferences() {
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
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String historyEntry = timestamp + " - " + purchasedItems;
        Set<String> historySet = sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>());
        Set<String> updatedHistory = new HashSet<>(historySet);
        if (!updatedHistory.contains(historyEntry)) {
            updatedHistory.add(historyEntry);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_HISTORY, updatedHistory);
        editor.apply();
        historyList.clear();
        historyList.addAll(updatedHistory);
        sortHistoryList();
        historyAdapter.notifyDataSetChanged();
    }

    private void sortHistoryList() {
        Collections.sort(historyList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    Date date1 = sdf.parse(o1.split(" - ")[0]);
                    Date date2 = sdf.parse(o2.split(" - ")[0]);
                    return date2.compareTo(date1);
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
            isHistoryVisible = !isHistoryVisible;
            if (isHistoryVisible) {
                if (historyList.isEmpty()) {
                    Toast.makeText(this, "Chưa có lịch sử thanh toán", Toast.LENGTH_SHORT).show();
                    isHistoryVisible = false;
                } else {
                    binding.historyContainer.setVisibility(View.VISIBLE); // Sử dụng historyContainer
                    binding.historyContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                    hasNotification = false;
                    updateBellIcon();
                }
            } else {
                Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.historyContainer.setVisibility(View.GONE); // Sử dụng historyContainer
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                binding.historyContainer.startAnimation(slideOut);
            }
        });
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);
        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Intent intent;
                if (i == R.id.favorites) {
                    intent = new Intent(MainActivity.this, FavoriteActivity.class);
                    startActivity(intent);
                } else if (i == R.id.cart) {
                    intent = new Intent(MainActivity.this, CartActivity.class);
                    startActivity(intent);
                } else if (i == R.id.profile) {
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                }
            }
        });
        binding.cartBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        allItems = new ArrayList<>();
        viewModel.loadPopular().observeForever(itemsModels -> {
            if (!itemsModels.isEmpty()) {
                allItems.clear();
                allItems.addAll(itemsModels);
                popularAdapter = new PopularAdapter(new ArrayList<>(allItems));
                binding.popularView.setLayoutManager(
                        new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                binding.popularView.setAdapter(popularAdapter);
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
            binding.categoryView.setAdapter(new CategoryAdapter(categoryModels, categoryTitle -> {
                filterItemsByCategory(categoryTitle);
            }));
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);

            if (!categoryModels.isEmpty()) {
                filterItemsByCategory("All");
            }
        });
    }

    private void filterItemsByCategory(String categoryTitle) {
        ArrayList<ItemsModel> filteredItems = new ArrayList<>();
        if (categoryTitle.equals("All")) {
            filteredItems.addAll(allItems);
        } else {
            String categoryToFilter = categoryTitle.equals("Men") ? "Man" : categoryTitle;
            for (ItemsModel item : allItems) {
                if (item.getCategory().equals(categoryToFilter)) {
                    filteredItems.add(item);
                }
            }
        }
        popularAdapter = new PopularAdapter(filteredItems);
        binding.popularView.setAdapter(popularAdapter);
        popularAdapter.notifyDataSetChanged();
    }

    private void loadUserNameFromFirebase() {
        Intent intent = getIntent();
        String usernameUser = intent.getStringExtra("username");
        if (usernameUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
            Query checkUserDatabase = reference.orderByChild("username").equalTo(usernameUser);
            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameFromDB = snapshot.child(usernameUser).child("name").getValue(String.class);
                        if (nameFromDB != null && !nameFromDB.isEmpty()) {
                            binding.textView5.setText(nameFromDB);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setItemSelected(R.id.home, true);
        loadUserNameFromFirebase();
    }
}