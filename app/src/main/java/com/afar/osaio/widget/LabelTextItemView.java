package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LabelTextItemView extends LinearLayout {

    @BindView(R.id.tvLabelTitle)
    TextView tvLabelTitle;
    @BindView(R.id.tvLabelRight_1)
    TextView tvLabelRight_1;
    @BindView(R.id.ivLabelArrow)
    ImageView ivLabelArrow;

    private boolean mIsDisableLabelRightAutoAlignment = false;

    public LabelTextItemView(Context context) {
        super(context, null);
    }

    public LabelTextItemView(Context context, AttributeSet attrs) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.layout_label_text_item, this, false);
        ButterKnife.bind(this, view);
        addView(view);
        monitorTextChange();
    }

    public LabelTextItemView setLabelTitle(String title) {
        if (tvLabelTitle != null) {
            tvLabelTitle.setText(title);
        }
        return this;
    }

    public LabelTextItemView setColor(int color) {
        if (tvLabelTitle != null && tvLabelRight_1 != null) {
            tvLabelTitle.setTextColor(color);
            tvLabelRight_1.setTextColor(color);
        }
        return this;
    }

    public LabelTextItemView setLabelRight_1(String text) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setText(text);
        }
        return this;
    }

    public String getTextLabelRight_1() {
        return tvLabelRight_1 != null ? tvLabelRight_1.getText().toString() : "";
    }

    public LabelTextItemView setLabelRight_Color_1(int color) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setTextColor(color);
        }
        return this;
    }

    public LabelTextItemView setLabelRightListener(OnClickListener listener) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setOnClickListener(listener);
        }
        return this;
    }

    public LabelTextItemView displayLabelRight_1(int visibility) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setVisibility(visibility);
        }
        return this;
    }

    public LabelTextItemView setArrowRes(int resId) {
        if (ivLabelArrow != null) {
            ivLabelArrow.setImageResource(resId);
        }
        return this;
    }

    public LabelTextItemView displayArrow(int visibility) {
        if (ivLabelArrow != null) {
            ivLabelArrow.setVisibility(visibility);
        }
        return this;
    }

    public LabelTextItemView setLabelWeight(int leftWeight, int rightWeight) {
        if (tvLabelTitle == null || tvLabelRight_1 == null || leftWeight < 0 || rightWeight < 0) {
            return this;
        }
        ConstraintLayout.LayoutParams tvLabelTitleLp = (ConstraintLayout.LayoutParams)tvLabelTitle.getLayoutParams();
        ConstraintLayout.LayoutParams tvLabelRightLp = (ConstraintLayout.LayoutParams)tvLabelRight_1.getLayoutParams();
        if (tvLabelTitleLp == null || tvLabelRightLp == null) {
            return this;
        }
        tvLabelTitleLp.horizontalWeight = leftWeight;
        tvLabelTitleLp.validate();
        tvLabelRightLp.horizontalWeight = rightWeight;
        tvLabelRightLp.validate();
        return this;
    }

    /**
     *
     * @param textAlignment Gravity.
     * @return
     */
    public LabelTextItemView setLabelRightAlignment(int textAlignment) {
        if (tvLabelRight_1 != null) {
            tvLabelRight_1.setGravity(textAlignment);
        }
        return this;
    }

    public LabelTextItemView setIsDisableLabelRightAutoAlignment(boolean disable) {
        mIsDisableLabelRightAutoAlignment = disable;
        return this;
    }

    public LabelTextItemView autoLabelRightAlignment() {
        if (tvLabelRight_1 != null) {
            //NooieLog.d("debug", "-->> debug LabelTextItemView autoLabelRightAlignment lineCount=" + tvLabelRight_1.getLineCount() + " textHeight=" + tvLabelRight_1.getHeight() + " text=" + tvLabelRight_1.getText());
            int textAlignment = tvLabelRight_1.getLineCount() > 1 ? Gravity.CENTER : Gravity.END;
            tvLabelRight_1.setGravity(textAlignment);
        }
        return this;
    }

    public void release() {
        tvLabelTitle = null;
        if (tvLabelRight_1 != null) {
            setLabelRightListener(null);
            tvLabelRight_1 = null;
        }
        ivLabelArrow = null;
        removeAllViews();
    }

    private void monitorTextChange() {
        if (tvLabelRight_1 == null) {
            return;
        }
        tvLabelRight_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mIsDisableLabelRightAutoAlignment) {
                    return;
                }
                autoLabelRightAlignment();
            }
        });
    }

}
