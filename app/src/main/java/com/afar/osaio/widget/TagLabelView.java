package com.afar.osaio.widget;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TagLabelView extends LinearLayout {

    @BindView(R.id.tvTagLabel)
    TextView tvTagLabel;
    @BindView(R.id.vTagLabelBottom)
    View vTagLabelBottom;

    public TagLabelView(Context context) {
        super(context, null);
    }

    public TagLabelView(Context context, AttributeSet attr) {
        super(context, attr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_tag_label, null, false);
        addView(view);
        ButterKnife.bind(this, view);
    }

    public TagLabelView setTextColor(int textColor) {
        if (tvTagLabel != null) {
            tvTagLabel.setTextColor(ContextCompat.getColor(getContext(), textColor));
        }
        return this;
    }

    public TagLabelView setTagBottomColor(int vColor) {
        if (vTagLabelBottom != null) {
            vTagLabelBottom.setBackgroundColor(ContextCompat.getColor(getContext(), vColor));
        }
        return this;
    }

    public TagLabelView setText(String text) {
        if (tvTagLabel != null) {
            tvTagLabel.setText(text);
        }
        return this;
    }

    public TagLabelView setTagSelected(boolean selected) {
        if (vTagLabelBottom != null) {
            vTagLabelBottom.setVisibility(selected ? VISIBLE : INVISIBLE);
        }
        return this;
    }

    public boolean isTagSelected() {
        return vTagLabelBottom != null && vTagLabelBottom.getVisibility() == VISIBLE;
    }
}
