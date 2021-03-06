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
     * L???ng nghe s??? ki???n b???m l??n giao di???n
     */
    private void initView() {
        //C???p nh???t th??ng tin cho text view tr??n actionbar
        ((M001ActMenu)mContext).setTvActionbar(getString(R.string.txt_action_alarm));

        //Hi???n th??? icon th??m b??o th???c
        ((M001ActMenu)mContext).showAddAlarm();

        //L???ng nghe s??? ki???n b???m l??n "Setup"
        binding.btnSetupAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //G???i h??m l??n l???ch b??o th???c
                SupportMethod.animButton(mContext,binding.btnSetupAlarm);
                setupAlarm();
            }
        });

        //L???ng nghe s??? ki???n b???m l??n text view "Date time"
        binding.tvDateTimeAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hi???n th??? dialog ch???n ng??y
                initDate();
            }
        });

    }

    /**
     * X??? l?? s??? ki???n ch???n ng??y tr??n DatePickerDialog
     */
    private void initDate() {
        //Th???i gian m???c ?????nh
        int selectYear = CAL.get(Calendar.YEAR);
        int selectMonth = CAL.get(Calendar.MONTH);
        int selectDayOfMonth = CAL.get(Calendar.DAY_OF_MONTH);

        //X??y d???ng ?????i t?????ng set ng??y
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                CAL.set(Calendar.YEAR, year);
                CAL.set(Calendar.MONTH, month);
                CAL.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //G???i h??m x??? l?? th???i gian
                initTime();
            }
        };

        //Hi???n th??? picker dialog
        DatePickerDialog pickerDialog = new DatePickerDialog(mContext, dateSetListener, selectYear,
                selectMonth, selectDayOfMonth);
        pickerDialog.show();
    }

    /**
     * X??? l?? s??? ki???n ch???n th???i gian tr??n TimePickerDialog
     */
    private void initTime() {
        //Th???i gian m???c ?????nh
        int selectHourOfDay = CAL.get(Calendar.HOUR_OF_DAY);
        int selectMinute = CAL.get(Calendar.MINUTE);

        //X??y d???ng ?????i t?????ng set th???i gian
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //Ch???n th???i gian
                CAL.set(Calendar.HOUR_OF_DAY, hourOfDay);
                CAL.set(Calendar.MINUTE, minute);

                //G???i h??m c???p nh???t th???i gian l??n giao di???n
                updateLabel();
            }
        };

        //Hi???n th??? picker dialog
        TimePickerDialog pickerDialog = new TimePickerDialog(mContext, timeSetListener,
                selectHourOfDay, selectMinute, true);
        pickerDialog.show();
    }

    /**
     * C???p nh???t ng??y v?? th???i gian l??n giao di???n
     */
    private void updateLabel() {
        //?????nh d???ng th???i gian
        String fm = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sf = new SimpleDateFormat(fm, Locale.US);

        //C???p nh???t th???i gian l??n giao di???n
        String text = sf.format(CAL.getTime());
        binding.tvDateTimeAlarm.setText(text);

        //G??n gi?? tr??? th???i gian theo ????n v??? mili gi??y cho textview
        binding.tvDateTimeAlarm.setTag(CAL.getTimeInMillis());
    }

    /**
     * T???o ?????i t?????ng th??ng tin cho Scheduler
     * @return JobInfo
     */
    private JobInfo creatJobInfo() {
        ComponentName componentService = new ComponentName(mContext, PendingService.class);
        //X??y d???ng ?????i t?????ng th??ng tin c??ng vi???c
        JobInfo.Builder infoBuilder = new JobInfo.Builder(rd.nextInt(Integer.MAX_VALUE), componentService );
        infoBuilder.setMinimumLatency(1000);
        infoBuilder.setOverrideDeadline(2000);
        infoBuilder.setRequiresCharging(false);

        //X??y d???ng ?????i t?????ng ????ng g??i th??ng tin
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.KEY_MSG, binding.etMsgAlarm.getText().toString());
        bundle.putLong(Constants.KEY_TIME, (long) binding.tvDateTimeAlarm.getTag());
        bundle.putString(Constants.KEY_TYPE, Constants.TYPE_ALARM);
        bundle.putLong(Constants.KEY_ID, (Long) binding.tvDateTimeAlarm.getTag());
        bundle.putString(Constants.KEY_STATE, Constants.STATE_NONE);

        //?????y bundle v??o ?????i t?????ng th??ng tin c??ng vi???c
        infoBuilder.setExtras(bundle);
        JobInfo jobInfo = infoBuilder.build();

        return jobInfo;
    }

    /**
     * T???o m???t ?????i t?????ng b??o th???c
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
     * Thi???t l???p h???n gi??? b??o th???c
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

        //T???o ?????i t?????ng th??ng tin cho ?????i JobScheduler
        JobInfo jobInfo = creatJobInfo();
        //T???o ?????i t?????ng qu???n l?? l???ch c??ng vi???c
        JobScheduler jobScheduler = (JobScheduler) (mContext).getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(jobInfo);

        //T???o m???t ?????i t?????ng AlarmEntity ???? l??u th??ng tin m???t b??o th???c v??o SharePreference
        AlarmEntity alarmEntity = createAlarmEntity();
        //G???i l??m l??u th??ng tin m???t ?????i t?????ng b??o th???c
        addData(alarmEntity);

        //M??? m??n h??nh 5 (Danh s??ch b??o th???c)
        ((M001ActMenu)mContext).replaceFrg(new M005ListAlarmFrg());
        Toast.makeText(mContext, "Alarm will woke up sometime", Toast.LENGTH_SHORT).show();

    }

    /**
     * L??u th??m m???t b??o th???c m???i v??o SharedPreference
     */
    public void addData(AlarmEntity alarmEntity) {
        if(alarmEntity == null){
            return;
        }
        //T???o ?????i t?????ng Gson ????? chuy???n ?????i d??? li???u
        Gson gson = new Gson();
        //Th??m b??o th???c v??o danh s??ch b??o th???c
        listAlarm.add(alarmEntity);
        //Chuy???n danh s??ch b??o th???c th??nh chu???i ????? l??u v??o SharedPreference
        String newTxtData = gson.toJson(listAlarm);

        //L??u danh s??ch b??o th???c v??o SharedPreference
        SharedPreferences pref = mContext.getSharedPreferences(Constants.KEY_PREF, Context.MODE_PRIVATE );
        pref.edit().putString(Constants.KEY_DATA, newTxtData).apply();
    }

}
