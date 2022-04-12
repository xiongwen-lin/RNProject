package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.TypedArray;
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

public class LabelActionItemView extends LinearLayout {

    @BindView(R.id.tvLabelTitle)
    TextView tvLabelTitle;
    @BindView(R.id.ivLabelArrow)
    ImageView ivLabelArrow;

    public LabelActionItemView(Context context) {
        super(context, null);
    }

    public LabelActionItemView(Context context, AttributeSet attrs) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.layout_label_action_item, this, false);
        ButterKnife.bind(this, view);
        addView(view);
    }

    public LabelActionItemView setLabelTitle(String title) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setText(title);
        }
        return this;
    }

    public LabelActionItemView setColor(int color) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setTextColor(color);
        }
        return this;
    }

    public LabelActionItemView setArrowRes(int resId) {
        if (ivLabelArrow != null) {
            ivLabelArrow.setImageResource(resId);
        }
        return this;
    }

    public LabelActionItemView displayArrow(int visibility) {
        if (ivLabelArrow != null) {
            ivLabelArrow.setVisibility(visibility);
        }
        return this;
    }

    public void release() {
        tvLabelTitle = null;
        ivLabelArrow = null;
        removeAllViews();
    }

}
