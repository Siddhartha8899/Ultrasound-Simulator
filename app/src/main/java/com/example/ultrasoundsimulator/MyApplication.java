package com.example.ultrasoundsimulator;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class MyApplication extends Application {
    private static MyApplication sInstance;

    public static MyApplication getApplication() {
        return sInstance;
    }

    public Storage storage;

    @Override
    public void onCreate() {
        super.onCreate();
        storage = loadData();
        if(storage == null) {
            storage = new Storage();
        }
        sInstance = this;
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(storage);
        editor.putString("storage_data", json);
        editor.apply();
    }

    public Storage loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("storage_data", null);
        Type type = new TypeToken<Storage>() {}.getType();
        return gson.fromJson(json,type);
    }
}
