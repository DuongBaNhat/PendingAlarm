package com.duongbanhat.pendingalarm.view.frg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.duongbanhat.pendingalarm.PendingService;
import com.duongbanhat.pendingalarm.R;
import com.duongbanhat.pendingalarm.adapter.Constants;
import com.duongbanhat.pendingalarm.databinding.M004FrgPhoneBinding;
import com.duongbanhat.pendingalarm.model.SupportMethod;
import com.duongbanhat.pendingalarm.view.act.M001ActMenu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class M004PhoneFrg extends Fragment {
    private static final String TAG = M004PhoneFrg.class.getName();
    private M004FrgPhoneBinding binding;
    private Context mContext;

    private final Random rd = new Random();
    private final Calendar CAL = Calendar.getInstance();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.m004_frg_phone, container, false);
        binding = M004FrgPhoneBinding.bind(rootView);

        initView();
        return rootView;
    }

    /**
     * X??? l?? s??? ki???n ch???n ng??y tr??n DatePickerDialog
     */
    private void initView() {
        //C???p nh???t th??ng tin cho text view tr??n actionbar
        ((M001ActMenu)mContext).setTvActionbar(getString(R.string.txt_action_phone));

        //X??? l?? tr??n text view "Date time"
        binding.tvDateTimeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //B???m v??o ?? th???i gian
                initDate();
            }
        });

        binding.btnSetupCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //G???i h??m l??n l???ch cu???c g???i
                SupportMethod.animButton(mContext,binding.btnSetupCall);

                setAlarm();
            }
        });

        checkSelfPermission();
    }

    /**
     * X??? l?? s??? ki???n ch???n ng??y tr??n DatePickerDialog
     */
    private void initDate() {
        //Ch???n ng??y
        //Ng??y m??c ?????nh
        int year = CAL.get(Calendar.YEAR);
        int month = CAL.get(Calendar.MONTH);
        int dayOfMonth = CAL.get(Calendar.DAY_OF_MONTH);

        //Ch???n ng??y t??? c???a s??? dialog
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                CAL.set(Calendar.YEAR, year);
                CAL.set(Calendar.MONTH, month);
                CAL.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //G???i h??m set th???i gian
                initTime();
            }
        };

        DatePickerDialog pickerDialog = new DatePickerDialog(mContext, dateSetListener,
                year, month, dayOfMonth);
        pickerDialog.show();
    }

    /**
     * X??? l?? s??? ki???n ch???n th???i gian tr??n TimePickerDialog
     */
    private void initTime() {
        //Ch???n th???i gian

        //Th???i gian m???c ?????nh
        int hourOfDay = CAL.get(Calendar.HOUR_OF_DAY);
        int minute = CAL.get(Calendar.MINUTE);

        //Bi???n set th???i gian cho CAL khi ch???n tr??n c???a s??? dialog
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
               CAL.set(Calendar.HOUR_OF_DAY, hourOfDay);
               CAL.set(Calendar.MINUTE, minute);

               //G???i h??m hi???n th??? th???i gian l??n m??n h??nh
                initUpdateTime();
            }
        };

        //Hi???n th??? c???a s??? dialog
        TimePickerDialog pickerDialog = new TimePickerDialog(mContext, timeSetListener, hourOfDay, minute, true);
        pickerDialog.show();

    }

    @SuppressLint("SimpleDateFormat")
    private void initUpdateTime() {
        String strFomart = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sf = new SimpleDateFormat(strFomart, Locale.US);
        String text = sf.format(CAL.getTime());
        binding.tvDateTimeCall.setText(text);
        binding.tvDateTimeCall.setTag(CAL.getTimeInMillis());
    }

    /**
     * Th???c hi???n l??n l???ch cu???c g???i
     */
    private void setAlarm() {
        if(binding.etCallTo.getText().toString().isEmpty()) {
            Toast.makeText(mContext, "Please fill phone number first", Toast.LENGTH_SHORT).show();
            return;
        }

        if(binding.tvDateTimeCall.getText().toString().isEmpty()) {
            Toast.makeText(mContext, "Please check setup time first", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = binding.etCallTo.getText().toString().trim();
        String time = binding.tvDateTimeCall.getText().toString().trim();
        boolean checkPhone = ((M001ActMenu)mContext).checkPhone(phone);
        if(phone.isEmpty()) {
            Toast.makeText(mContext, "Please fill phone number first", Toast.LENGTH_SHORT).show();
            return;
        }
        if(time.isEmpty()) {
            Toast.makeText(mContext, "Please check setup time first", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!checkPhone) {
            Toast.makeText(mContext, "The phone is not correct, please check!", Toast.LENGTH_SHORT).show();
            return;
        }
        //X??y d???ng l???ch
        ComponentName componentService = new ComponentName(mContext, PendingService.class);
        JobInfo.Builder builder = new JobInfo.Builder(rd.nextInt(Integer.MAX_VALUE), componentService);

        builder.setRequiresCharging(false);
        builder.setMinimumLatency(1000);
        builder.setOverrideDeadline(2000);

        //T???o ?????i t?????ng ????? ?????y th??ng tin v??o
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.KEY_PHONE, binding.etCallTo.getText().toString().trim());
        bundle.putLong(Constants.KEY_TIME, (long) binding.tvDateTimeCall.getTag());
        bundle.putString(Constants.KEY_TYPE, Constants.TYPE_PHONE);

        //Kh???i t???o JobInfo
        builder.setExtras(bundle);
        JobInfo jobInfo = builder.build();

        //Kh???i t???o JobScheduler
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);

        //M??? m??n h??nh 1
        ((M001ActMenu)mContext).startCloseMenu(true, false);

        Toast.makeText(mContext, "A call will done sometime", Toast.LENGTH_SHORT).show();

    }

    /**
     * Ki???m tra v?? y??u c???u c???p quy???n th???c hi???n cu???c g???i
     */
    private void checkSelfPermission() {
        String permission = Manifest.permission.CALL_PHONE;
        int checkSelf = ContextCompat.checkSelfPermission(mContext, permission );
        if (checkSelf != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[] {permission}, Constants.CODE_PHONE);
        }
    }

    /**
     * L???ng nghe ph???n h???i t??? ng?????i d??ng
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.CODE_PHONE) {
            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Please allow permission for using it", Toast.LENGTH_SHORT).show();
                ((M001ActMenu)mContext).startCloseMenu(true, false);

            }
        }
    }
}
