package com.assignment.androidshoppingapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.assignment.androidshoppingapp.Domain.BannerModel;
import com.assignment.androidshoppingapp.Domain.CategoryModel;
import com.assignment.androidshoppingapp.Domain.ItemsModel;
import com.assignment.androidshoppingapp.Respository.MainRepository;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MainRepository repository = new MainRepository();

    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        return repository.loadCategory();
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        return repository.loadBanner();
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        return repository.loadPopular();
    }
}
