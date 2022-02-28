package com.duongbanhat.pendingalarm.view.frg;

import android.Manifest;
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
import com.duongbanhat.pendingalarm.databinding.M002FrgSmsBinding;
import com.duongbanhat.pendingalarm.model.SupportMethod;
import com.duongbanhat.pendingalarm.view.act.M001ActMenu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;


public class M002SMSFrg extends Fragment {
    public static final String TAG = M002SMSFrg.class.getName();

    private final Random rd = new Random();
    private final Calendar CAL = Calendar.getInstance();
    private Context mContext;
    private M002FrgSmsBinding binding;

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
        binding = M002FrgSmsBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        initView();

        return rootView;
    }

    /**
     * Lắng nghe sự kiện bấm lên giao diện
     */
    private void initView() {
        //Cập nhật thông tin cho text view trên actionbar
        ((M001ActMenu)mContext).setTvActionbar(getString(R.string.txt_action_sms));
        //Bấm lên nút Setup
        binding.btSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SupportMethod.animButton(mContext,binding.btSetup);
                setupAlarm();
            }
        });

        //Bấm lên textview thời gian
        binding.tvDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDate();
            }
        });
        //Kiểm tra quyền
        checkSelfPermission();
    }

    /**
     * Xử lý sự kiện chọn ngày trên DatePickerDialog
     */
    private void initDate() {
        //Ngày mặc định
        int selectedYear = CAL.get(Calendar.YEAR);
        int selectedMonth = CAL.get(Calendar.MONTH);
        int selectedDayOfMonth = CAL.get(Calendar.DAY_OF_MONTH);

        // Gán dữ liệu chon biến CAL khi chọn trên giao diện DatePickerDialog
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                CAL.set(Calendar.YEAR, year);
                CAL.set(Calendar.MONTH, monthOfYear);
                CAL.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //Gọi hàm xử lý thời gian
                initTime();
            }
        };

        // Khởi tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();

    }

    /**
     * Xử lý sự kiện chọn thời gian trên TimePickerDialog
     */
    private void initTime() {
        //Thời gian mặc định
        int selectedHourOfDay = CAL.get(Calendar.HOUR_OF_DAY);
        int selectedMinute = CAL.get(Calendar.MINUTE);
        //Gán thời gian được chọn trên TimePickerDialog cho biến CAL
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                CAL.set(Calendar.HOUR_OF_DAY, hourOfDay);
                CAL.set(Calendar.MINUTE, minute);

                //Gọi hàm cập nhật ngày và thời gian lên text view trên giao diện
                updateLabel();
            }
        };

        //Khởi tạo và hiển thị của sổ TimePickerDialog
        TimePickerDialog dialog = new TimePickerDialog(mContext, timeSetListener, selectedHourOfDay,
                selectedMinute, true);
        dialog.show();
    }

    /**
     * Cập nhật ngày và thời gian lên giao diện
     */
    private void updateLabel() {
        //Định dạng ngày và thời gian
        String format = "dd-MM-yyyy HH:mm";
        SimpleDateFormat sf = new SimpleDateFormat(format, Locale.US);
        String text = sf.format(CAL.getTime());

        //Cập nhật lên giao diện
        binding.tvDateTime.setText(text);
        binding.tvDateTime.setTag(CAL.getTimeInMillis());
    }

    /**
     * Thiết lập hẹn giờ gửi tin nhắn
     */
    private void setupAlarm() {
        Log.i(TAG, "setupAlarm");

        String phone = binding.etPhone.getText().toString().trim();
        String msg = binding.etMsg.toString();
        String time = binding.tvDateTime.getText().toString().trim();

        //Kiểm tra thông tin đã phù hợp chưa
        if(phone.isEmpty() || msg.isEmpty()){
            Toast.makeText(mContext, "Please check your information!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!((M001ActMenu)mContext).checkPhone(phone)) {
            Toast.makeText(mContext, "The phone is not correct, please check!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(time.isEmpty()){
            Toast.makeText(mContext, "Please check setup time first", Toast.LENGTH_SHORT).show();
            return;
        }


        //Tạo đối tượng quản lý thông tin
        ComponentName serviceComponent = new ComponentName(mContext, PendingService.class);
        JobInfo.Builder builder = new JobInfo.Builder(rd.nextInt(Integer.MAX_VALUE), serviceComponent);
        builder.setMinimumLatency(1000);
        builder.setOverrideDeadline(2 * 1000);
        builder.setRequiresCharging(false);

        //Tạo đối tượng để đóng gói thông tin
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.KEY_TYPE, Constants.TYPE_SMS );
        bundle.putString(Constants.KEY_PHONE, binding.etPhone.getText().toString());
        bundle.putString(Constants.KEY_MSG, binding.etMsg.getText().toString());
        bundle.putLong(Constants.KEY_TIME, (long) binding.tvDateTime.getTag());

        //Đẩy thông tin vào đối tượng quản lý thông tin
        builder.setExtras(bundle);

        //Tạo đối tượng lên lịch
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(builder.build());

        //Mở màn hình 1
        ((M001ActMenu)mContext).startCloseMenu(true, false);
        Toast.makeText(mContext, "A message will be sent sometime", Toast.LENGTH_SHORT).show();

    }

    /**
     * Kiểm tra quyền gửi tin nhắn
     */
    private void checkSelfPermission() {
        String strPermission = Manifest.permission.SEND_SMS;
        int checkSelf = ContextCompat.checkSelfPermission(mContext,strPermission);
       if(checkSelf != PackageManager.PERMISSION_GRANTED ) {
           requestPermissions(new String[] {strPermission}, Constants.SMS_CODE);
       }

    }

    /**
     * Lắng nghe phản hồi từ người dùng
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.SMS_CODE){
            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Please allow permission for using it", Toast.LENGTH_SHORT).show();
                ((M001ActMenu)mContext).startCloseMenu(true, false);

            }
        }
    }


}
