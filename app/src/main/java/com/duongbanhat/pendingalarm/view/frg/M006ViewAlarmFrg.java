package com.duongbanhat.pendingalarm.view.frg;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.duongbanhat.pendingalarm.PendingService;
import com.duongbanhat.pendingalarm.R;
import com.duongbanhat.pendingalarm.adapter.AlarmEntity;
import com.duongbanhat.pendingalarm.adapter.ApplicationClass;
import com.duongbanhat.pendingalarm.adapter.Constants;
import com.duongbanhat.pendingalarm.databinding.M006FrgAlarmBinding;
import com.duongbanhat.pendingalarm.view.act.M001ActMenu;

import java.util.List;

public class M006ViewAlarmFrg extends Fragment {
    private static final String TAG = M006ViewAlarmFrg.class.getName();
    private Context mContext;
    private M006FrgAlarmBinding binding;
    private List<AlarmEntity> listAlarm;
    private AlarmEntity alarmEntity;
    private Intent intent;

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
        binding = M006FrgAlarmBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        listAlarm = ApplicationClass.listAlarm; //Lấy danh sách báo thức lưu trong SharedPreference
        initView();
        return view;
    }

    /**
     * Thiết lập và lắng nghe sự kiện trên giao diện báo thức
     */
    private void initView() {
        Log.i(TAG, "initView");
        //Cập nhật thông tin cho text view trên actionbar
        ((M001ActMenu)mContext).setTvActionbar(getString(R.string.txt_action_alarm));
        //Cập nhật tin nhắn lên giao diện
        binding.tvMessage.setText(intent.getStringExtra(Constants.KEY_MSG));
        long id = intent.getLongExtra(Constants.KEY_ID, 0);

        //Tham chiếu đến báo thức hiện tại trong danh sách
        for (AlarmEntity alarm : listAlarm){
            if(alarm.getId() == id) {
                alarmEntity = alarm;
            }
        }
        //Lắng nghe sự kiện bấm lên "Stop"
        binding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(alarmEntity != null){
                    Log.i(TAG, "initView2");
                    //Thiết lập lại trạng thái của báo thức
                    alarmEntity.setState(Constants.STATE_STOP);
                    //Lưu vào SharedPreference
                    ((M001ActMenu)mContext).saveData(listAlarm);

                    //Gọi lại thiết lập hẹn giờ báo thức sau 1 phút
                    restartAlarm();
                }

            }
        });

        //Lắng nghe sự kiện bấm lên "Done"
        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alarmEntity != null){
                    alarmEntity.setState(Constants.STATE_DONE);
                    ((M001ActMenu)mContext).saveData(listAlarm);

                    //Mở màn hình 5 (Danh sách báo thức)
                    ((M001ActMenu)mContext).replaceFrg(new M005ListAlarmFrg());

                }
            }
        });
    }

    /**
     * Nhận thông tin gửi từ activity (M001ActMenu)
     * @param intent
     */
    public void receiverData(Intent intent){
        Log.i(TAG, "receiverData");
        this.intent = intent;
    }

    /**
     * Tạo báo thức sau 1 phút
     */
    private void restartAlarm() {
        //Báo thức sau 1 phút
        long plusTime = 60000;
        //Khởi tạo đối tượng thông tin JobInfo
        ComponentName jobService = new ComponentName(mContext, PendingService.class);
        //Xây dựng đối tượng thông tin công việc
        JobInfo.Builder infoBuilder = new JobInfo.Builder(1101, jobService );
        infoBuilder.setMinimumLatency(1000);
        infoBuilder.setOverrideDeadline(2000);
        infoBuilder.setRequiresCharging(false);

        //Đóng gói thông tin
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.KEY_MSG, intent.getStringExtra(Constants.KEY_MSG));
        bundle.putLong(Constants.KEY_TIME, System.currentTimeMillis() + plusTime);
        bundle.putString(Constants.KEY_TYPE, Constants.TYPE_ALARM);
        bundle.putLong(Constants.KEY_ID, intent.getLongExtra(Constants.KEY_ID, 0));

        //Đẩy bundle vào đối tượng thông tin công việc
        infoBuilder.setExtras(bundle);
        JobInfo jobInfo = infoBuilder.build();

        //Tạo đối tượng quản lý lịch công việc
        JobScheduler jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        //Lên lịch
        jobScheduler.schedule(jobInfo);

        //Mở màn hình 5 (Danh sách báo thức)
        ((M001ActMenu)mContext).replaceFrg(new M005ListAlarmFrg());
        Toast.makeText(mContext, "Alarm will woke up sometime", Toast.LENGTH_SHORT).show();
    }

}
