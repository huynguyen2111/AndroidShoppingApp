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
    private PopularAdapter popularAdapter; // Thêm biến để lưu adapter
    private ArrayList<ItemsModel> allItems; // Lưu toàn bộ danh sách sản phẩm

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
        sortHistoryList();

        historyAdapter = new HistoryAdapter(historyList);
        binding.historyView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyView.setAdapter(historyAdapter);

        // Thêm chức năng vuốt để xóa
        setupSwipeToDelete();

        // Lấy dữ liệu từ Intent và cập nhật tên người dùng
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

        // Thêm sự kiện click cho icon profile (imageView2)
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
                    binding.historyView.setVisibility(View.VISIBLE);
                    binding.historyView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
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
        allItems = new ArrayList<>(); // Khởi tạo danh sách toàn bộ sản phẩm
        viewModel.loadPopular().observeForever(itemsModels -> {
            if (!itemsModels.isEmpty()) {
                allItems.clear();
                allItems.addAll(itemsModels); // Lưu toàn bộ sản phẩm
                popularAdapter = new PopularAdapter(new ArrayList<>(allItems)); // Hiển thị toàn bộ sản phẩm ban đầu
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
            // Truyền listener để xử lý sự kiện nhấn danh mục
            binding.categoryView.setAdapter(new CategoryAdapter(categoryModels, categoryTitle -> {
                filterItemsByCategory(categoryTitle); // Lọc sản phẩm theo danh mục
            }));
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }

    // Hàm lọc sản phẩm theo danh mục
    private void filterItemsByCategory(String categoryTitle) {
        ArrayList<ItemsModel> filteredItems = new ArrayList<>();
        if (categoryTitle.equals("All")) {
            filteredItems.addAll(allItems); // Hiển thị toàn bộ sản phẩm
        } else {
            // Lọc sản phẩm theo danh mục
            String categoryToFilter = categoryTitle.equals("Men") ? "Man" : categoryTitle; // Chuyển "Men" thành "Man"
            for (ItemsModel item : allItems) {
                if (item.getCategory().equals(categoryToFilter)) {
                    filteredItems.add(item);
                }
            }
        }
        // Cập nhật adapter với danh sách đã lọc
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
                    // Xử lý lỗi nếu cần
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