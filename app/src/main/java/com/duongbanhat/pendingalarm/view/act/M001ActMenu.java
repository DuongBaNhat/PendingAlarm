package com.duongbanhat.pendingalarm.view.act;


import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.duongbanhat.pendingalarm.PendingService;
import com.duongbanhat.pendingalarm.R;
import com.duongbanhat.pendingalarm.adapter.AlarmAdapter;
import com.duongbanhat.pendingalarm.adapter.AlarmEntity;
import com.duongbanhat.pendingalarm.adapter.ApplicationClass;
import com.duongbanhat.pendingalarm.adapter.Constants;
import com.duongbanhat.pendingalarm.databinding.M001ActMenuBinding;
import com.duongbanhat.pendingalarm.databinding.ViewDialogBinding;
import com.duongbanhat.pendingalarm.view.frg.M002SMSFrg;
import com.duongbanhat.pendingalarm.view.frg.M003AlarmFrg;
import com.duongbanhat.pendingalarm.view.frg.M004PhoneFrg;
import com.duongbanhat.pendingalarm.view.frg.M005ListAlarmFrg;
import com.duongbanhat.pendingalarm.view.frg.M006ViewAlarmFrg;
import com.google.gson.Gson;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M001ActMenu extends AppCompatActivity implements View.OnClickListener , AlarmAdapter.IItemClick {
    private static final String TAG = M001ActMenu.class.getName();
    private M001ActMenuBinding binding;
    @SuppressLint("StaticFieldLeak")
    private List<AlarmEntity> listAlarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        binding = M001ActMenuBinding.inflate(inflater);

        View rootView = binding.getRoot();
        setContentView(rootView);

        //Tải danh sách báo thức từ SharedPreference
        listAlarm = ApplicationClass.listAlarm;

        addEvents();
        addAlarm();
    }

    /**
     * Thêm sự kiện
     */
    private void addEvents() {
        binding.trSms.setOnClickListener(M001ActMenu.this);
        binding.trCall.setOnClickListener(M001ActMenu.this);
        binding.trAlarm.setOnClickListener(M001ActMenu.this);
        binding.actionBar.ivMenu.setOnClickListener(M001ActMenu.this);
        binding.actionBar.ivAddAlarm.setOnClickListener(M001ActMenu.this);
        //Lắng nghe trạng trái của menu để thiết lập icon trên actionbar
        binding.dlMenu.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                binding.actionBar.ivMenu.setImageResource(R.drawable.ic_menu_open_24);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                binding.actionBar.ivMenu.setImageResource(R.drawable.ic_menu_close_24);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    /**
     * Xử lý sự kiện nhấn lên menu, actionbar
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.tr_sms:
                M002SMSFrg smsFrg = new M002SMSFrg();
                replaceFrg(smsFrg);

                break;
            case R.id.tr_call:
                M004PhoneFrg callFrg = new M004PhoneFrg();
                replaceFrg(callFrg);
                break;
            case R.id.tr_alarm:
                M005ListAlarmFrg m005ListAlarmFrg = new M005ListAlarmFrg();
                replaceFrg(m005ListAlarmFrg);
                break;
            case R.id.iv_add_alarm:
                M003AlarmFrg m003AlarmFrg = new M003AlarmFrg();
                replaceFrg(m003AlarmFrg);

                break;

            case R.id.iv_menu:
                startCloseMenu(false, true);
                break;
            default:
                break;
        }
    }

    /**
     * Sự kiện click trên item view của recycler view
     * @param index
     */
    @Override
    public void onItemClick(int index) {
        AlarmEntity alarmEntity = listAlarm.get(index);
        if(alarmEntity == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        ViewDialogBinding bindingDialog = ViewDialogBinding.inflate(inflater);

        View view = bindingDialog.getRoot();

        //Xây dựng AlertDialog và hiển thị
        AlertDialog dialog = dialogShow(bindingDialog, view, alarmEntity);

        //Xử lý sự kiện trên dialog
        bindingDialog.btnDone.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"ResourceAsColor", "NotifyDataSetChanged"})
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Cập nhật trạng thái của báo thức
                alarmEntity.setState(Constants.STATE_DONE);
                //Hiển thị danh sách báo thức
                replaceFrg(new M005ListAlarmFrg());
                //Lưu lại báo thức
                saveData(listAlarm);
            }
        });

        bindingDialog.btnRemove.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                listAlarm.remove(alarmEntity);
                //Hiển thị danh sách báo thức
                replaceFrg(new M005ListAlarmFrg());

                //Lưu lại báo thức
                saveData(listAlarm);

            }
        });
    }

    /**
     * Thay thế fragment
     * @param frg
     */
    public void replaceFrg(Fragment frg) {
        //Ẩn icon thêm báo thức
        binding.actionBar.ivAddAlarm.setVisibility(View.GONE);
        binding.flHome.removeViewInLayout(binding.tvHello);
        //Đối tượng quản lý fragment
        FragmentManager manager = getSupportFragmentManager();
        //Đối tượng xử lý hoạt động của fragment
        FragmentTransaction transaction = manager.beginTransaction();
        //Set các thông tin cho khởi dộng fragment
        transaction.setCustomAnimations(R.anim.left_in, R.anim.leftout, R.anim.right_in, R.anim.right_out);
        //Đưa fragment vào Stack để tái sử dụng
        transaction.addToBackStack(null);
        //Thay thế fragment
        transaction.replace(R.id.fl_home, frg);
        //Xác nhận lệnh
        transaction.commit();
        //Đóng mở menu
        startCloseMenu(false, false);

    }

    /**
     * Xây đựng diolog
     * @param bindingDialog
     * @param view
     * @param alarmEntity
     * @return AlertDialog
     */
    private AlertDialog dialogShow(ViewDialogBinding bindingDialog, View view, AlarmEntity alarmEntity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);


        //Cập nhật thông tin trên cửa sổ dialog
        bindingDialog.tvTitle.setText(R.string.txt_alarm);
        String desc = alarmEntity.getMessage();
        bindingDialog.tvDesc.setText(desc);

        AlertDialog dialog = builder.create();
//        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return dialog;
    }

    /**
     * Xử lý nhận báo thức
     */
    private void addAlarm() {
        Log.i(TAG, "addAlarm: ");
        Intent intent = M001ActMenu.this.getIntent();
        if(intent == null) {
            return;
        }

        //Kiểm tra có phải là intent của báo thức hay không
        String type = intent.getStringExtra(Constants.KEY_TYPE);
        if(type == null || !type.equals(Constants.TYPE_ALARM)) {
            return;
        }
        M006ViewAlarmFrg m006ViewAlarmFrg = new M006ViewAlarmFrg();
        replaceFrg(m006ViewAlarmFrg);
        m006ViewAlarmFrg.receiverData(intent);

    }

    /**
     * Lưu danh sách báo thức vào SharedPreference
     * @param listAlarm
     */
    public void saveData(List<AlarmEntity> listAlarm) {
        SharedPreferences pref = getSharedPreferences(Constants.KEY_PREF, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(listAlarm);
        pref.edit().putString(Constants.KEY_DATA, json).apply();
    }

    /**
     * Hiển thị icon thêm báo thức
     */
    public void showAddAlarm(){
        binding.actionBar.ivAddAlarm.setVisibility(View.VISIBLE);
    }

    /**
     * Thiết lập title trên thanh actionbar
     * @param title
     */
    public void setTvActionbar(String title) {
        binding.actionBar.tvActionbar.setText(title);
    }

    /**
     * Đóng mở menu
     */
    public void startCloseMenu(boolean isDrawerOpen, boolean auto){
        if(auto){
            if(binding.dlMenu.isDrawerOpen(GravityCompat.START)){
                binding.dlMenu.closeDrawer(GravityCompat.START);

            } else {
                binding.dlMenu.openDrawer(GravityCompat.START);
            }
        } else {
            if(isDrawerOpen) {
                binding.dlMenu.openDrawer(GravityCompat.START);
            } else {
                binding.dlMenu.closeDrawer(GravityCompat.START);
            }
        }


    }

    /**
     * Xử lý hành động bấm lên nut back của thiết bị
     */
    @Override
    public void onBackPressed() {
        if(binding.dlMenu.isDrawerOpen(GravityCompat.START)){
            binding.dlMenu.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();

    }

    /**
     * Kiểm tra định dạng của số điện thoại
     * @param phone
     * @return
     */
    public boolean checkPhone(String phone) {
        //Định dạng số điện thoại phù hợp
        String regex = "^0\\d{9}$";
        String regex1 = "5554";

        //Kiểm tra số điện thoại
        Patterns.PHONE.matcher(phone).matches();

        //Kết quả kiểm tra
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phone);

        Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(phone);
        return matcher.find() || matcher1.find();
    }
}