package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LabelSwTagItemView extends LinearLayout {

    @BindView(R.id.tvLabelTitle)
    TextView tvLabelTitle;
    @BindView(R.id.tvLabelTag)
    TextView tvLabelTag;
    @BindView(R.id.swLabelRight)
    SwitchButton swLabelRight;

    public LabelSwTagItemView(Context context) {
        super(context, null);
    }

    public LabelSwTagItemView(Context context, AttributeSet attrs) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.layout_label_sw_tag_item, this, false);
        ButterKnife.bind(this, view);
        addView(view);
    }

    public LabelSwTagItemView setLabelTitle(String title) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setText(title);
        }
        return this;
    }

    public LabelSwTagItemView setColor(int color) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setTextColor(color);
        }
        return this;
    }

    public LabelSwTagItemView setLabelTag(String tag) {
        if (tvLabelTag != null) {
            tvLabelTag.setText(tag);
        }
        return this;
    }

    public LabelSwTagItemView setTagColor(int color) {
        if (tvLabelTag != null) {
            tvLabelTag.setTextColor(color);
        }
        return this;
    }

    public LabelSwTagItemView displayLabelRightSw(int visibility) {
        if (swLabelRight != null) {
            swLabelRight.setVisibility(visibility);
        }
        return this;
    }

    public LabelSwTagItemView displayLabelTag(int visibility) {
        if (tvLabelTag != null) {
            tvLabelTag.setVisibility(visibility);
        }
        return this;
    }

    public LabelSwTagItemView setLabelRightSwListener(SwitchButton.OnCheckedChangeListener listener) {
        if (swLabelRight != null) {
            swLabelRight.setOnCheckedChangeListener(listener);
        }
        return this;
    }

    public LabelSwTagItemView toggleLabelRightSw() {
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
