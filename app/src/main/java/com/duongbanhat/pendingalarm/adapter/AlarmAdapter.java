package com.duongbanhat.pendingalarm.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.duongbanhat.pendingalarm.R;
import com.duongbanhat.pendingalarm.databinding.ItemAlarmBinding;

import java.util.List;

/**
 * Tạo một AlarmAdapter để định nghĩa một giao diện báo thức trong danh sách
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmHolder> {
    private List<AlarmEntity> listAlarm;
    private Context mContext;
    private LayoutInflater inflater;
    private IItemClick activity;

    /**
     * Interface để lấy thông tin từ 1 view được click
     */
    public interface IItemClick {
        void  onItemClick(int index);
    }

    public AlarmAdapter(Context mContext, List<AlarmEntity> listAlarm){
        this.mContext = mContext;
        this.listAlarm = listAlarm;
        inflater = LayoutInflater.from(mContext); //Chuyển layout xml thành view trong java code
        activity = (IItemClick) mContext;
    }

    /**
     * Phương thức này trả về một viewholder đã thiết lập kết nối với dữ liệu (nhưng chưa có dữ liệu)
     * @param parent
     * @param viewType
     * @return ViewHolder (AlarmHolder)
     */
    @NonNull
    @Override
    public AlarmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_alarm, parent, false);
        //Lắng nghe sự kiện bấm trên một view item
        view.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                AlarmEntity alarmEntity = (AlarmEntity) view.getTag();
                activity.onItemClick(listAlarm.indexOf(alarmEntity));
            }
        });

        return new AlarmHolder(view);
    }

    /**
     * Thiết lập dữ liệu cho một view item lấy ra từ phương thức onCreateViewHolder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull AlarmHolder holder, int position) {
        AlarmEntity alarmEntity = listAlarm.get(position);
        if(alarmEntity == null){
            return;
        }
        //Thiết lập dữ liệu cho một item
        holder.binding.tvTitle.setText(alarmEntity.getMessage());
        holder.binding.tvTimeItem.setText(alarmEntity.getTime());
        if(alarmEntity.getState().equals(Constants.STATE_DONE)) {
            holder.binding.ivItem.setBackgroundColor(mContext.getColor(R.color.purple_500));
        } else if(alarmEntity.getState().equals(Constants.STATE_STOP)) {
            holder.binding.ivItem.setBackgroundColor(mContext.getColor(R.color.red_500));
        } else {
            holder.binding.ivItem.setBackgroundColor(mContext.getColor(R.color.orange_500));
        }

        holder.itemView.setTag(alarmEntity);
    }

    /**
     * Số lượng alarm trong danh sách
     * @return
     */
    @Override
    public int getItemCount() {
        return listAlarm.size();
    }

    /**
     * Lớp liên kết dữ liệu với view item (chỉ tạo liên kết, chưa có dữ liệu)
     */
    public class AlarmHolder extends RecyclerView.ViewHolder {
        ItemAlarmBinding binding;
        public AlarmHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAlarmBinding.bind(itemView);
        }
    }
}
