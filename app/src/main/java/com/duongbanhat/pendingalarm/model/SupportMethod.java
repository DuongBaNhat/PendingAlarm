package com.duongbanhat.pendingalarm.model;

import android.app.Activity;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.duongbanhat.pendingalarm.R;

//check
public class SupportMethod {
    public SupportMethod(){}
    /**
     * Animation khi click nút
     * @param mContext
     * @param button Nút muốn tạo hiệu ứng
     */
    public static void animButton(Context mContext, Button button) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.alpha_fade_out);
        button.startAnimation(animation);
    }
}
