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

public class LabelTagItemView extends LinearLayout {

    @BindView(R.id.tvLabelTitle)
    TextView tvLabelTitle;
    @BindView(R.id.ivLabelTitleTag)
    ImageView ivLabelTitleTag;
    @BindView(R.id.tvLabelRight_1)
    TextView tvLabelRight_1;
    @BindView(R.id.ivLabelArrow)
    ImageView ivLabelArrow;

    public LabelTagItemView(Context context) {
        super(context, null);
    }

    public LabelTagItemView(Context context, AttributeSet attrs) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.layout_label_tag_item, this, false);
        ButterKnife.bind(this, view);
        addView(view);
    }

    public LabelTagItemView setLabelTitle(String title) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setText(title);
        }
        return this;
    }

    public LabelTagItemView setColor(int color) {
        if (tvLabelTitle != null && tvLabelRight_1 != null) {
            tvLabelTitle.setTextColor(color);
            tvLabelRight_1.setTextColor(color);
        }
        return this;
    }

    public LabelTagItemView setLabelRight_1(String text) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setText(text);
        }
        return this;
    }

    public LabelTagItemView setLabelRight_Color_1(int color) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setTextColor(color);
        }
        return this;
    }

    public LabelTagItemView setLabelRightListener(OnClickListener listener) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setOnClickListener(listener);
        }
        return this;
    }

    public LabelTagItemView displayLabelRight_1(int visibility) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setVisibility(visibility);
        }
        return this;
    }

    public LabelTagItemView setLabelTitleTag(int resId) {
        if (ivLabelTitleTag != null) {
            ivLabelTitleTag.setImageResource(resId);
        }
        return this;
    }

    public LabelTagItemView displayLabelTitleTag(int visibility) {
        if (ivLabelTitleTag != null) {
            ivLabelTitleTag.setVisibility(visibility);
        }
        return this;
    }

    public LabelTagItemView setArrowRes(int resId) {
        if (ivLabelArrow != null) {
            ivLabelArrow.setImageResource(resId);
        }
        return this;
    }

    public LabelTagItemView displayArrow(int visibility) {
        if (ivLabelArrow != null) {
            ivLabelArrow.setVisibility(visibility);
        }
        return this;
    }

    public void release() {
        tvLabelTitle = null;
        ivLabelTitleTag = null;
        tvLabelRight_1 = null;
        ivLabelArrow = null;
        removeAllViews();
    }

}
