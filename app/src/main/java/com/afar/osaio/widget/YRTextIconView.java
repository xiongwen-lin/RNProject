package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class YRTextIconView extends LinearLayout {

    @BindView(R.id.ivTextIcon)
    ImageView ivTextIcon;
    @BindView(R.id.tvTextTitle)
    TextView tvTextTitle;

    public YRTextIconView(Context context) {
        super(context);
        init();
    }

    public YRTextIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_yr_text_icon, this, false);
        addView(lvpView);
        bindView(lvpView);
    }

    public void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public YRTextIconView setTextIcon(int resId) {
        if (ivTextIcon != null) {
            ivTextIcon.setImageResource(resId);
        }
        return this;
    }

    public YRTextIconView setTextIconBg(int resId) {
        if (ivTextIcon != null) {
            if (resId == 0) {
                ivTextIcon.setBackground(null);
            } else {
                ivTextIcon.setBackgroundResource(resId);
            }
        }
        return this;
    }

    public YRTextIconView setTextTitle(String title) {
        if (tvTextTitle != null) {
            tvTextTitle.setText(title);
        }
        return this;
    }

    public YRTextIconView setTextTitleColor(int colorId) {
        if (tvTextTitle != null) {
            tvTextTitle.setTextColor(ContextCompat.getColor(getContext(), colorId));
        }
        return this;
    }

    public void release() {
        ivTextIcon = null;
        tvTextTitle = null;
        removeAllViews();
    }
}
