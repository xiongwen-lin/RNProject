package com.afar.osaio.widget.base;

import android.content.Context;

import com.afar.osaio.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BaseBottomSheetDialog extends BottomSheetDialog {

    public BaseBottomSheetDialog(Context context) {
        super(context, R.style.BottomSheetEdit);
    }

    public void setContainerTransparentBackground() {
        findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
    }

    public void setContainerBackground(int resId) {
        findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundResource(resId);
    }

    public void setOutsideBackground(int resId) {
        findViewById(com.google.android.material.R.id.touch_outside).setBackgroundResource(resId);
    }
}
