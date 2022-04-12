package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LabelSwItemView extends LinearLayout {

    @BindView(R.id.tvLabelTitle)
    TextView tvLabelTitle;
    @BindView(R.id.swLabelRight)
    SwitchButton swLabelRight;

    public LabelSwItemView(Context context) {
        super(context, null);
    }

    public LabelSwItemView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initView(context);
        initAttr(context, attrs);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LabelItemView, 0, 0);
        setLabelTitle(a.getString(R.styleable.LabelItemView_title));
        setColor(a.getColor(R.styleable.LabelItemView_color, ContextCompat.getColor(getContext(), R.color.theme_text_color)));
        if (a != null) {
            a.recycle();
        }
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_label_sw_item, this, false);
        ButterKnife.bind(this, view);
        addView(view);
    }

    public LabelSwItemView setLabelTitle(String title) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setText(title);
        }
        return this;
    }

    public LabelSwItemView setColor(int color) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setTextColor(color);
        }
        return this;
    }

    public LabelSwItemView displayLabelRightSw(int visibility) {
        if (swLabelRight != null) {
            swLabelRight.setVisibility(visibility);
        }
        return this;
    }

    public LabelSwItemView setLabelRightSwListener(SwitchButton.OnCheckedChangeListener listener) {
        if (swLabelRight != null) {
            swLabelRight.setOnCheckedChangeListener(listener);
        }
        return this;
    }

    public LabelSwItemView toggleLabelRightSw() {
        if (swLabelRight != null) {
            swLabelRight.toggleNoCallback();
        }
        return this;
    }

    public boolean isLabelRightSwCheck() {
        return swLabelRight != null && swLabelRight.isChecked();
    }

    public void release() {
        tvLabelTitle = null;
        if (swLabelRight != null) {
            swLabelRight.setOnCheckedChangeListener(null);
            swLabelRight = null;
        }
        removeAllViews();
    }

}
