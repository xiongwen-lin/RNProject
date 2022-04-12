package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NormalTextIconView extends LinearLayout {

    @BindView(R.id.ivTextIcon)
    ImageView ivTextIcon;
    @BindView(R.id.tvTextTitle)
    TextView tvTextTitle;

    public NormalTextIconView(Context context) {
        super(context);
        init();
    }

    public NormalTextIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_normal_text_icon, this, false);
        addView(lvpView);
        bindView(lvpView);
    }

    public void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public NormalTextIconView setTextIcon(int resId) {
        if (ivTextIcon != null) {
            ivTextIcon.setImageResource(resId);
        }
        return this;
    }

    public NormalTextIconView setTextTitle(String title) {
        if (tvTextTitle != null) {
            tvTextTitle.setText(title);
        }
        return this;
    }

    public void setIvIconEnable(boolean enable) {
        setEnabled(enable);
        if (ivTextIcon != null) {
            ivTextIcon.setEnabled(enable);
        }
        if (tvTextTitle != null) {
            tvTextTitle.setEnabled(enable);
        }
    }

    public void release() {
        ivTextIcon = null;
        tvTextTitle = null;
        removeAllViews();
    }
}
