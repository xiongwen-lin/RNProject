package com.afar.osaio.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.afar.osaio.R;

/**
 * Create by Raymond  on 2021/6/24
 * Description:
 */
public class SetingsBackupDialog extends Dialog {
    private Context context;
    private ConfirmListener listener;

    public SetingsBackupDialog(@NonNull Context context, ConfirmListener listener) {
        super(context, R.style.Dialog);
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = View.inflate(context, R.layout.dialog_only_item, null);
        setContentView(view);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(view1 -> {
            listener.confirmListener();
            dismiss();
        });
    }


    public interface ConfirmListener {
        void confirmListener();
    }

}
