package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NormalTextAnimIconView extends LinearLayout {

    @BindView(R.id.ivTextIcon)
    IconAnimView ivTextIcon;
    @BindView(R.id.tvTextTitle)
    TextView tvTextTitle;

    public NormalTextAnimIconView(Context context) {
        super(context);
        init();
    }

    public NormalTextAnimIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_normal_text_anim_icon, this, false);
        addView(lvpView);
        bindView(lvpView);
    }

    public void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public void setTextTitle(String title) {
        if (tvTextTitle != null) {
            tvTextTitle.setText(title);
        }
    }

    public void setupView(int levelRes) {
        if (ivTextIcon != null) {
            ivTextIcon.setupView(levelRes);
        }
    }

    public void setIvIconOnOrOff(boolean on) {
        if (ivTextIcon != null) {
            ivTextIcon.setIvIconOnOrOff(on);
        }
    }

    public void setIvIconEnable(boolean enable) {
        if (ivTextIcon != null) {
            ivTextIcon.setIvIconEnable(enable);
        }
    }

    public void resetIvIcon() {
        if (ivTextIcon != null) {
            ivTextIcon.resetIvIcon();
        }
    }

    public void runStartIconAnim() {
        if (ivTextIcon != null) {
            ivTextIcon.runStartIconAnim();
        }
    }

    public void runEndIconAnim() {
        if (ivTextIcon != null) {
            ivTextIcon.runEndIconAnim();
        }
    }

    public void release() {
        if (ivTextIcon != null) {
            ivTextIcon.release();
            ivTextIcon = null;
        }
        tvTextTitle = null;
    }
}
