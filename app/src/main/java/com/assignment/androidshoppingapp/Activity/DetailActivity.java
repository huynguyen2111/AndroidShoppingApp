package com.assignment.androidshoppingapp.Activity;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.assignment.androidshoppingapp.Adapter.ColorAdapter;
import com.assignment.androidshoppingapp.Adapter.PicListAdapter;
import com.assignment.androidshoppingapp.Adapter.SizeAdapter;
import com.assignment.androidshoppingapp.Domain.ItemsModel;
import com.assignment.androidshoppingapp.Helper.ManagmentCart;
import com.assignment.androidshoppingapp.databinding.ActivityDetailBinding;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private ItemsModel object;
    private int numberOrder = 1;
    private ManagmentCart managmentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);

        getBundles();
        initPicList();
        initSize();
        initColor();

    }

    private void initColor() {
        binding.recyclerColor.setAdapter(new ColorAdapter(object.getColor()));
        binding.recyclerColor.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void initSize() {
        binding.recyclerSize.setAdapter(new SizeAdapter(object.getSize()));
        binding.recyclerSize.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
    }

    private void initPicList() {
        ArrayList<String> picList = new ArrayList<>(object.getPicUrl());

        Glide.with(this)
                .load(picList.get(0))
                .into(binding.pic);

        binding.picList.setAdapter(new PicListAdapter(picList, binding.pic));
        binding.picList.
                setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false));
    }

    private void getBundles() {
        object = (ItemsModel) getIntent().getSerializableExtra("object");
        binding.titleTxt.setText(object.getTitle());
        binding.priceTxt.setText("$" + object.getPrice());
        binding.oldPriceTxt.setText("$" + object.getOldPrice());
        binding.oldPriceTxt.setPaintFlags(binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        binding.descriptionTxt.setText(object.getDescription());

        binding.addToCartBtn.setOnClickListener(v -> {
            object.setNumberinCart(numberOrder);
            managmentCart.insertItem(object);
        });

        binding.backBtn.setOnClickListener(v -> finish());
    }
}