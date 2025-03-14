package com.assignment.androidshoppingapp.Helper;

import android.content.Context;
import com.assignment.androidshoppingapp.Domain.ItemsModel;
import java.util.ArrayList;

public class ManagementFavorites {
    private TinyDB tinyDB;
    private static final String FAVORITE_KEY = "FavoriteList";

    public ManagementFavorites(Context context) {
        this.tinyDB = new TinyDB(context);
    }

    public void addFavorite(ItemsModel item) {
        ArrayList<ItemsModel> favorites = getListFavorites();
        if (!isFavorite(item)) {
            favorites.add(item);
            tinyDB.putListObject(FAVORITE_KEY, favorites);
        }
    }

    public void removeFavorite(ItemsModel item) {
        ArrayList<ItemsModel> favorites = getListFavorites();
        favorites.removeIf(i -> i.getTitle().equals(item.getTitle())); // Remove by title match
        tinyDB.putListObject(FAVORITE_KEY, favorites);
    }

    public boolean isFavorite(ItemsModel item) {
        ArrayList<ItemsModel> favorites = getListFavorites();
        for (ItemsModel fav : favorites) {
            if (fav.getTitle().equals(item.getTitle())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ItemsModel> getListFavorites() {
        return tinyDB.getListObject(FAVORITE_KEY);
    }
}
