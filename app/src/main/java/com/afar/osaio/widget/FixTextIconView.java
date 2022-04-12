package com.afar.osaio.widget;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FixTextIconView extends LinearLayout {

    @BindView(R.id.ivTextIcon)
    ImageView ivTextIcon;
    @BindView(R.id.tvTextTitle)
    TextView tvTextTitle;

    public FixTextIconView(Context context) {
        super(context);
        init();
    }

    public FixTextIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_fix_text_icon, this, false);
        addView(lvpView);
        bindView(lvpView);
    }

    public void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public FixTextIconView setTextIcon(int resId) {
        if (ivTextIcon != null) {
            ivTextIcon.setImageResource(resId);
        }
        return this;
    }

    public FixTextIconView setTextIconBg(int resId) {
        if (ivTextIcon != null) {
            if (resId == 0) {
                ivTextIcon.setBackground(null);
            } else {
                ivTextIcon.setBackgroundResource(resId);
            }
        }
        return this;
    }

    public FixTextIconView setTextTitle(String title) {
        if (tvTextTitle != null) {
            tvTextTitle.setText(title);
        }
        return this;
    }

    public FixTextIconView setTextTitleColor(int colorId) {
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
