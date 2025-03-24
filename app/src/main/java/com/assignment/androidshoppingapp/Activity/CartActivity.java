package com.assignment.androidshoppingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.assignment.androidshoppingapp.Adapter.CartAdapter;
import com.assignment.androidshoppingapp.Adapter.FavoriteAdapter;
import com.assignment.androidshoppingapp.Domain.ItemsModel;
import com.assignment.androidshoppingapp.Helper.ManagmentCart;
import com.assignment.androidshoppingapp.databinding.ActivityCartBinding;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private double tax;
    private ManagmentCart managmentCart;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);

        setVariable();
        initCartList();
        calculatorCart();

        // Thêm sự kiện cho nút Check out
        binding.checkoutBtn.setOnClickListener(v -> {
            if (managmentCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy danh sách các mặt hàng trong giỏ
            String purchasedItems = getPurchasedItems();
            // Hiển thị thông báo thanh toán thành công
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            // Tạo Intent để gửi dữ liệu về MainActivity
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.putExtra("purchased_items", purchasedItems);
            intent.putExtra("checkout_success", true); // Đánh dấu thanh toán thành công
            startActivity(intent);
            // Xóa giỏ hàng sau khi thanh toán
            managmentCart.clearCart();
            // Cập nhật giao diện ngay lập tức
            initCartList();
            calculatorCart();
            finish(); // Đóng CartActivity
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Làm mới giao diện khi quay lại CartActivity
        initCartList();
        calculatorCart();
    }

    private void setupFavoriteList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView3.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView3.setVisibility(View.VISIBLE);
        }

        binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.cartView.setAdapter(cartAdapter);
    }

    private String getPurchasedItems() {
        StringBuilder items = new StringBuilder();
        for (ItemsModel item : managmentCart.getListCart()) {
            items.append(item.getTitle()).append(" (x").append(item.getNumberinCart()).append("), ");
        }
        // Xóa dấu ", " cuối cùng nếu có
        if (items.length() > 2) {
            items.setLength(items.length() - 2);
        }
        return items.toString();
    }

    private void initCartList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView3.setVisibility(View.GONE);
            binding.checkoutBtn.setEnabled(false); // Vô hiệu hóa nút Check out nếu giỏ hàng trống
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView3.setVisibility(View.VISIBLE);
            binding.checkoutBtn.setEnabled(true); // Kích hoạt nút Check out
        }

        if (cartAdapter == null) {
            cartAdapter = new CartAdapter(managmentCart.getListCart(), this, this::calculatorCart);
            binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.cartView.setAdapter(cartAdapter);
        } else {
            cartAdapter.notifyDataSetChanged(); // Cập nhật adapter nếu đã tồn tại
        }
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void calculatorCart() {
        double percentTax = 0.02;
        double delivery = 10;
        tax = Math.round((managmentCart.getTotalFee() * percentTax * 100.0)) / 100.0;

        double total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100.0) / 100.0;
        double itemTotal = Math.round((managmentCart.getTotalFee() * 100.0)) / 100.0;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.taxTxt.setText("$" + tax);
        binding.deliveryTxt.setText("$" + delivery);
        binding.totalTxt.setText("$" + total);
    }
}