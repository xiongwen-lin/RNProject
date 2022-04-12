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
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afar.osaio.R;

/**
 * Create by Raymond  on 2021/6/23
 * Description:  固件升级Dialog
 */
public class FirmwareUpdateInfoDialog extends Dialog {
    private Context context;
    private String updateVersion;
    private String updateContent;
    private UpdateListener listener;


    public FirmwareUpdateInfoDialog(@NonNull Context context, String updateContent,
                                    String updateVersion, UpdateListener listener) {
        super(context, R.style.Dialog);
        this.context = context;
        this.updateContent = updateContent;
        this.updateVersion = updateVersion;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(context, R.layout.dialog_routter_firmware_update_info, null);
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

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvVersion.setText(updateVersion);
        tvMessage.setText(updateContent);

        btnCancel.setOnClickListener(view1 -> {
            dismiss();
        });

        btnOk.setOnClickListener(view1 -> {
            dismiss();
            listener.confirmUpdate();
        });

    }

    public interface UpdateListener {
        void confirmUpdate();
    }


}
