package com.afar.osaio.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.configure.FontUtil;

/**
 * Created by victor on 2018/7/2
 * Email is victor.qiao.0604@gmail.com
 */
public class FButton extends AppCompatButton {

    public FButton(Context context) {
        super(context);
        initTypeface();
    }

    public FButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypeface();
    }

    public FButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypeface();
    }

    private void initTypeface() {
        Typeface typeface = FontUtil.loadTypeface(NooieApplication.mCtx, "fonts/Avenir.ttc");
        setTypeface(typeface);

        TextPaint tp = getPaint();
        tp.setFakeBoldText(true);
        setEllipsize(TextUtils.TruncateAt.END);
        setMaxLines(2);
    }
}
