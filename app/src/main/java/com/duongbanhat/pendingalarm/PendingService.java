package com.duongbanhat.pendingalarm;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.duongbanhat.pendingalarm.adapter.AlarmEntity;
import com.duongbanhat.pendingalarm.adapter.ApplicationClass;
import com.duongbanhat.pendingalarm.adapter.Constants;
import com.duongbanhat.pendingalarm.view.act.M001ActMenu;
import com.duongbanhat.pendingalarm.view.frg.M002SMSFrg;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

public class PendingService extends JobService {
    public static final String TAG = PendingService.class.getName();

    private JobAsyncTask mJobAsyncTask;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "PendingService onStartJob");

        //Khởi tạo lớp quản lý các tác vụ để chạy các tác vụ
        //gọi executeOnExecutor để thực hiện song song các tác vụ
        mJobAsyncTask = new JobAsyncTask();
        mJobAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobAsyncTask.cancel(false);
        return false;
    }

    /**
     * Các tác vụ muốn chạy
     * @param params
     */
    private void doPendingTask(JobParameters params) {
        Log.i(TAG,"doPendingTask");

        //Cần các thông tin: thời điểm, nội dung tin nhắn, số điện thoại, loại dịch vụ
        String type = params.getExtras().getString(Constants.KEY_TYPE);
        long time = params.getExtras().getLong(Constants.KEY_TIME);
        long timeNow = Calendar.getInstance().getTimeInMillis();
        long timeDelay = time - timeNow;
        Log.i(TAG, "timeDelay " + timeDelay);

        if (timeDelay < 0){
            return;
        }

        //Tạm hoãn một khoảng thời gian trước khi thực hiện các tác vụ
        try {
            Thread.sleep(timeDelay);
        } catch (Exception e) {
            Log.i(TAG, "Exception timeDelay: " + timeDelay + "\n" + "Exception: " + e.getMessage());
            return;
        }


        if(type.equals(Constants.TYPE_SMS)) {
            sendSMS(params);
        } else if (type.equals(Constants.TYPE_PHONE)) {
            makeCall(params);
        } else if (type.equals(Constants.TYPE_ALARM)) {
            makeAnAlarm(params);
        } else {
            return;
        }

    }

    /**
     * Xử lý báo thức
     * @param params
     */
    private void makeAnAlarm(JobParameters params){
        String state = Constants.STATE_DONE;
        long id = params.getExtras().getLong(Constants.KEY_ID);

        for (AlarmEntity alarmEntity : ApplicationClass.listAlarm){
            if(alarmEntity.getId() == id){
                state = alarmEntity.getState();
                break;
            }
        }

        if(state.equals(Constants.STATE_DONE)){
            return;
        }

        String msg = params.getExtras().getString(Constants.KEY_MSG);
        //Xây dựng intent để truyền thông tin
        Intent intent = new Intent(this, M001ActMenu.class);
        intent.putExtra(Constants.KEY_MSG, msg);
        intent.putExtra(Constants.KEY_TYPE, Constants.TYPE_ALARM);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.KEY_ID, id);

        //Khởi chạy activity
        startActivity(intent);

    }

    /**
     * Xử lý cuộc gọi
     * @param params
     */
    private void makeCall(JobParameters params) {
        Log.i(TAG, "makeCall");
        String phone = params.getExtras().getString(Constants.KEY_PHONE);
        //Tạo intent để truyền thông tin
        Uri uri = Uri.parse("tel:" + phone);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        //Khởi chạy activity
        startActivity(intent);
    }

    /**
     * Xử lý gửi tin nhắn
     * @param params
     */
    private void sendSMS(JobParameters params) {
        Log.i(TAG, "sendSMS");
        String msg = params.getExtras().getString(Constants.KEY_MSG);
        String phone = params.getExtras().getString(Constants.KEY_PHONE);
        Log.i(TAG, "sendSMS " + msg +  " to " + phone);

        //Dùng lớp SmsManger để thực hiện gửi tin nhắn
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, msg, null, null);
    }

    /**
     *     Triển khai lớp AsyncTask để quản lý các tác vụ
     *     Lớp này sẽ tạo ra một Thread mới tránh trình trạng treo ứng dụng
     *     Cung cấp các tác vụ như cập nhật tiến độ, trả về kết quả khi công việc kết thúc
     */
    @SuppressLint("StaticFieldLeak")
    private class JobAsyncTask extends AsyncTask<JobParameters, Long, JobParameters> {
        public JobAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            long time;
            if(!isCancelled()){
                doPendingTask(jobParameters[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            super.onPostExecute(jobParameters);
        }

    }
}
