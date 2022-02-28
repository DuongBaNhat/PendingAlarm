package com.duongbanhat.pendingalarm.view.frg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duongbanhat.pendingalarm.R;
import com.duongbanhat.pendingalarm.adapter.AlarmAdapter;
import com.duongbanhat.pendingalarm.adapter.AlarmEntity;
import com.duongbanhat.pendingalarm.adapter.ApplicationClass;
import com.duongbanhat.pendingalarm.adapter.Constants;
import com.duongbanhat.pendingalarm.databinding.M005FrgListAlarmBinding;
import com.duongbanhat.pendingalarm.view.act.M001ActMenu;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * màn hình danh sách báo thức MH-5
 */
public class M005ListAlarmFrg extends Fragment {
    public static final String TAG = M005ListAlarmFrg.class.getName();
    private M005FrgListAlarmBinding binding;
    private Context mContext;
    private AlarmAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        binding = M005FrgListAlarmBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        initView();

        return rootView;
    }

    /**
     * Tải dữ liệu trong bộ nhớ SharePreference và hiển thị lên giao diện
     */
    private void initView() {
        Log.i(TAG, "initView");

        //Cập nhật thông tin cho text view trên actionbar
        ((M001ActMenu)mContext).setTvActionbar(getString(R.string.txt_action_alarm));


        //Tải dữ liệu từ SharedPreference
        List<AlarmEntity> listAlarm = ApplicationClass.listAlarm;

        //Chuyển danh sách báo thức lên Recycler View
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        adapter = new AlarmAdapter(mContext, listAlarm);

        binding.rvAlarm.setLayoutManager(layoutManager);
        binding.rvAlarm.setAdapter(adapter);

        //Hiển thị icon thêm báo thức
        ((M001ActMenu)mContext).showAddAlarm();
    }
}
