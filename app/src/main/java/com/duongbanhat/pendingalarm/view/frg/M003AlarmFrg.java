package com.duongbanhat.pendingalarm.view.frg;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.duongbanhat.pendingalarm.PendingService;
import com.duongbanhat.pendingalarm.R;
import com.duongbanhat.pendingalarm.adapter.AlarmEntity;
import com.duongbanhat.pendingalarm.adapter.ApplicationClass;
import com.duongbanhat.pendingalarm.adapter.Constants;
import com.duongbanhat.pendingalarm.databinding.M003FrgAlarmBinding;
import com.duongbanhat.pendingalarm.model.SupportMethod;
import com.duongbanhat.pendingalarm.view.act.M001ActMenu;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class M003AlarmFrg extends Fragment {
    private static final String TAG = M003AlarmFrg.class.getName();

    private final Calendar CAL = Calendar.getInstance();
    private final Random rd = new Random();

    private M003FrgAlarmBinding binding;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private List<AlarmEntity> listAlarm;

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

        View rootView = inflater.inflate(R.layout.m003_frg_alarm, container, false);
        binding = M003FrgAlarmBinding.bind(rootView);
        listAlarm = ApplicationClass.listAlarm;
        initView();
        return rootView;
    }

    /**
     * Lắng nghe sự kiện bấm lên giao diện
     */
    private void initView() {
        //Cập nhật thông tin cho text view trên actionbar
        ((M001ActMenu)mContext).setTvActionbar(getString(R.string.txt_action_alarm));

        //Hiển thị icon thêm báo thức
        ((M001ActMenu)mContext).showAddAlarm();

        //Lắng nghe sự kiện bấm lên "Setup"
        binding.btnSetupAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Gọi hàm lên lịch báo thức
                SupportMethod.animButton(mContext,binding.btnSetupAlarm);
                setupAlarm();
            }
        });

        //Lắng nghe sự kiện bấm lên text view "Date time"
        binding.tvDateTimeAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hiển thị dialog chọn ngày
                initDate();
            }
        });

    }

    /**
     * Xử lý sự kiện chọn ngày trên DatePickerDialog
     */
    private void initDate() {
        //Thời gian mặc định
        int selectYear = CAL.get(Calendar.YEAR);
        int selectMonth = CAL.get(Calendar.MONTH);
        int selectDayOfMonth = CAL.get(Calendar.DAY_OF_MONTH);

        //Xây dựng đối tượng set ngày
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                CAL.set(Calendar.YEAR, year);
                CAL.set(Calendar.MONTH, month);
                CAL.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //Gọi hàm xử lý thời gian
                initTime();
            }
        };

        //Hiển thị picker dialog
        DatePickerDialog pickerDialog = new DatePickerDialog(mContext, dateSetListener, selectYear,
                selectMonth, selectDayOfMonth);
        pickerDialog.show();
    }

    /**
     * Xử lý sự kiện chọn thời gian trên TimePickerDialog
     */
    private void initTime() {
        //Thời gian mặc định
        int selectHourOfDay = CAL.get(Calendar.HOUR_OF_DAY);
        int selectMinute = CAL.get(Calendar.MINUTE);

        //Xây dựng đối tượng set thời gian
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //Chọn thời gian
                CAL.set(Calendar.HOUR_OF_DAY, hourOfDay);
                CAL.set(Calendar.MINUTE, minute);

                //Gọi hàm cập nhật thời gian lên giao diện
                updateLabel();
            }
        };

        //Hiện thị picker dialog
        TimePickerDialog pickerDialog = new TimePickerDialog(mContext, timeSetListener,
                selectHourOfDay, selectMinute, true);
        pickerDialog.show();
    }

    /**
     * Cập nhật ngày và thời gian lên giao diện
     */
    private void updateLabel() {
        //Định dạng thời gian
        String fm = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sf = new SimpleDateFormat(fm, Locale.US);

        //Cập nhật thời gian lên giao diện
        String text = sf.format(CAL.getTime());
        binding.tvDateTimeAlarm.setText(text);

        //Gán giá trị thời gian theo đơn vị mili giây cho textview
        binding.tvDateTimeAlarm.setTag(CAL.getTimeInMillis());
    }

    /**
     * Tạo đối tượng thông tin cho Scheduler
     * @return JobInfo
     */
    private JobInfo creatJobInfo() {
        ComponentName componentService = new ComponentName(mContext, PendingService.class);
        //Xây dựng đối tượng thông tin công việc
        JobInfo.Builder infoBuilder = new JobInfo.Builder(rd.nextInt(Integer.MAX_VALUE), componentService );
        infoBuilder.setMinimumLatency(1000);
        infoBuilder.setOverrideDeadline(2000);
        infoBuilder.setRequiresCharging(false);

        //Xây dụng đối tượng đóng gói thông tin
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.KEY_MSG, binding.etMsgAlarm.getText().toString());
        bundle.putLong(Constants.KEY_TIME, (long) binding.tvDateTimeAlarm.getTag());
        bundle.putString(Constants.KEY_TYPE, Constants.TYPE_ALARM);
        bundle.putLong(Constants.KEY_ID, (Long) binding.tvDateTimeAlarm.getTag());
        bundle.putString(Constants.KEY_STATE, Constants.STATE_NONE);

        //Đẩy bundle vào đối tượng thông tin công việc
        infoBuilder.setExtras(bundle);
        JobInfo jobInfo = infoBuilder.build();

        return jobInfo;
    }

    /**
     * Tạo một đối tượng báo thức
     * @return AlarmEntity
     */
    private AlarmEntity createAlarmEntity(){
        AlarmEntity alarmEntity = new AlarmEntity();

        alarmEntity.setId(CAL.getTimeInMillis());
        alarmEntity.setYear(CAL.get(Calendar.YEAR));
        alarmEntity.setMonth(CAL.get(Calendar.MONTH));
        alarmEntity.setDay(CAL.get(Calendar.DAY_OF_MONTH));
        alarmEntity.setHour(CAL.get(Calendar.HOUR_OF_DAY));
        alarmEntity.setMinute(CAL.get(Calendar.MINUTE));
        alarmEntity.setMessage(binding.etMsgAlarm.getText().toString());
        alarmEntity.setTime(binding.tvDateTimeAlarm.getText().toString());
        alarmEntity.setState(Constants.STATE_NONE);
        alarmEntity.setLongTime(CAL.getTimeInMillis());

        return alarmEntity;
    }

    /**
     * Thiết lập hẹn giờ báo thức
     */
    private void setupAlarm() {
        Log.i(TAG, "alarmEntity1");

        if(binding.etMsgAlarm.getText().toString().isEmpty()) {
            Toast.makeText(mContext, "Please fill message first", Toast.LENGTH_SHORT).show();
            return;
        }
        if(binding.tvDateTimeAlarm.getText().toString().isEmpty() || CAL.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(mContext, "Please check setup time first", Toast.LENGTH_SHORT).show();
            return;
        }

        //Tạo đối tượng thông tin cho đối JobScheduler
        JobInfo jobInfo = creatJobInfo();
        //Tạo đối tượng quản lý lịch công việc
        JobScheduler jobScheduler = (JobScheduler) (mContext).getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(jobInfo);

        //Tạo một đối tượng AlarmEntity đê lưu thông tin một báo thức vào SharePreference
        AlarmEntity alarmEntity = createAlarmEntity();
        //Gọi làm lưu thông tin một đối tượng báo thức
        addData(alarmEntity);

        //Mở màn hình 5 (Danh sách báo thức)
        ((M001ActMenu)mContext).replaceFrg(new M005ListAlarmFrg());
        Toast.makeText(mContext, "Alarm will woke up sometime", Toast.LENGTH_SHORT).show();

    }

    /**
     * Lưu thêm một báo thức mới vào SharedPreference
     */
    public void addData(AlarmEntity alarmEntity) {
        if(alarmEntity == null){
            return;
        }
        //Tạo đối tượng Gson để chuyển đổi dữ liệu
        Gson gson = new Gson();
        //Thêm báo thức vào danh sách báo thức
        listAlarm.add(alarmEntity);
        //Chuyển danh sách báo thức thành chuỗi để lưu vào SharedPreference
        String newTxtData = gson.toJson(listAlarm);

        //Lưu danh sách báo thức vào SharedPreference
        SharedPreferences pref = mContext.getSharedPreferences(Constants.KEY_PREF, Context.MODE_PRIVATE );
        pref.edit().putString(Constants.KEY_DATA, newTxtData).apply();
    }

}
