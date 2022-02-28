package com.duongbanhat.pendingalarm.adapter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.nio.channels.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lớp chứa danh sách alarm cho toàn ứng dụng
 */
public class ApplicationClass extends Application {
    public static List<AlarmEntity> listAlarm;

    @Override
    public void onCreate() {
        super.onCreate();
        loadDataFromSharedPreference();
    }

    private void loadDataFromSharedPreference() {
        listAlarm = new ArrayList<>();

        SharedPreferences pref = getSharedPreferences(Constants.KEY_PREF, Context.MODE_PRIVATE);
        if(pref == null) {
            return;
        }
        String txtJSon = pref.getString(Constants.KEY_DATA, null);
        if(txtJSon == null){
            return;
        }
        Gson gson = new Gson();
        AlarmEntity[] jSonArr = gson.fromJson(txtJSon, AlarmEntity[].class);
        listAlarm.addAll(Arrays.asList(jSonArr));
    }
}
