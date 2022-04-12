package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepeatActivity extends BaseActivity {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivSun)
    ImageView ivSun;
    @BindView(R.id.ivMon)
    ImageView ivMon;
    @BindView(R.id.ivTues)
    ImageView ivTues;
    @BindView(R.id.ivWed)
    ImageView ivWed;
    @BindView(R.id.ivThurs)
    ImageView ivThurs;
    @BindView(R.id.ivFri)
    ImageView ivFri;
    @BindView(R.id.ivSat)
    ImageView ivSat;
    @BindView(R.id.tvSun)
    TextView tvSun;
    @BindView(R.id.tvMon)
    TextView tvMon;
    @BindView(R.id.tvTues)
    TextView tvTues;
    @BindView(R.id.tvWed)
    TextView tvWed;
    @BindView(R.id.tvThurs)
    TextView tvThurs;
    @BindView(R.id.tvFri)
    TextView tvFri;
    @BindView(R.id.tvSat)
    TextView tvSat;
    @BindView(R.id.tvRepeatTip)
    TextView tvRepeatTip;

    private ArrayList<String> selectDate;
    private StringBuffer sbValue;
    private boolean isEffect;
    private String loops;

    public static void toRepeatActivity(Activity from, int requestCode, boolean isEffect) {
        Intent intent = new Intent(from, RepeatActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_REPEAT, requestCode);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_EFFECT, isEffect);
        from.startActivityForResult(intent, requestCode);
    }

    public static void toRepeatActivity(Activity from, int requestCode, boolean isEffect, String loops) {
        Intent intent = new Intent(from, RepeatActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_REPEAT, requestCode);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_EFFECT, isEffect);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENE_LOOP, loops);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_repeat);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.repeat));
        selectDate = new ArrayList<>();
        sbValue = new StringBuffer();
        if (!isEffect) {
            tvRepeatTip.setVisibility(View.VISIBLE);
        }
        if (isEffect) {
            if (TextUtils.isEmpty(loops)) {
                allSelected();
            }
        }
    }

    private void allSelected() {
        ivSun.setSelected(true);
        ivMon.setSelected(true);
        ivTues.setSelected(true);
        ivWed.setSelected(true);
        ivThurs.setSelected(true);
        ivFri.setSelected(true);
        ivSat.setSelected(true);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            isEffect = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_EFFECT, false);
            loops = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENE_LOOP);
            if (!TextUtils.isEmpty(loops) && loops.length() == 7) {
                char[] chars = loops.toCharArray();
                for (int i = 0; i < chars.length; i++) {

                    if (i == 0 && chars[i] == '1') {
                        ivSun.setSelected(true);
                    }

                    if (i == 1 && chars[i] == '1') {
                        ivMon.setSelected(true);
                    }

                    if (i == 2 && chars[i] == '1') {
                        ivTues.setSelected(true);
                    }

                    if (i == 3 && chars[i] == '1') {
                        ivWed.setSelected(true);
                    }

                    if (i == 4 && chars[i] == '1') {
                        ivThurs.setSelected(true);
                    }

                    if (i == 5 && chars[i] == '1') {
                        ivFri.setSelected(true);
                    }

                    if (i == 6 && chars[i] == '1') {
                        ivSat.setSelected(true);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (ivSun.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvSun.getText().toString());
        } else {
            sbValue.append("0");
        }
        if (ivMon.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvMon.getText().toString());
        } else {
            sbValue.append("0");
        }
        if (ivTues.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvTues.getText().toString());
        } else {
            sbValue.append("0");
        }
        if (ivWed.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvWed.getText().toString());
        } else {
            sbValue.append("0");
        }
        if (ivThurs.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvThurs.getText().toString());
        } else {
            sbValue.append("0");
        }
        if (ivFri.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvFri.getText().toString());
        } else {
            sbValue.append("0");
        }
        if (ivSat.isSelected()) {
            sbValue.append("1");
            selectDate.add(tvSat.getText().toString());
        } else {
            sbValue.append("0");
        }
        NooieLog.e("-------------selectDate " + selectDate.size() + "  value = " + sbValue.toString());
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ConstantValue.INTENT_KEY_REPEAT, selectDate);
        intent.putExtra(ConstantValue.INTENT_KEY_REPEAT_VALUE, sbValue.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick({R.id.ivLeft, R.id.llSun, R.id.llMon, R.id.llTues, R.id.llWed, R.id.llThus, R.id.llFri, R.id.llSat})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                if (ivSun.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvSun.getText().toString());
                } else {
                    sbValue.append("0");
                }
                if (ivMon.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvMon.getText().toString());
                } else {
                    sbValue.append("0");
                }
                if (ivTues.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvTues.getText().toString());
                } else {
                    sbValue.append("0");
                }
                if (ivWed.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvWed.getText().toString());
                } else {
                    sbValue.append("0");
                }
                if (ivThurs.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvThurs.getText().toString());
                } else {
                    sbValue.append("0");
                }
                if (ivFri.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvFri.getText().toString());
                } else {
                    sbValue.append("0");
                }
                if (ivSat.isSelected()) {
                    sbValue.append("1");
                    selectDate.add(tvSat.getText().toString());
                } else {
                    sbValue.append("0");
                }
                NooieLog.e("-------------selectDate " + selectDate.size() + "  value = " + sbValue.toString());
                Intent intent = new Intent();
                intent.putStringArrayListExtra(ConstantValue.INTENT_KEY_REPEAT, selectDate);
                intent.putExtra(ConstantValue.INTENT_KEY_REPEAT_VALUE, sbValue.toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
            }
            case R.id.llSun: {
                if (ivSun.isSelected()) {
                    ivSun.setSelected(false);
                } else {
                    ivSun.setSelected(true);
                }
                break;
            }
            case R.id.llMon: {
                if (ivMon.isSelected()) {
                    ivMon.setSelected(false);
                } else {
                    ivMon.setSelected(true);
                }
                break;
            }
            case R.id.llTues: {
                if (ivTues.isSelected()) {
                    ivTues.setSelected(false);
                } else {
                    ivTues.setSelected(true);
                }
                break;
            }
            case R.id.llWed: {
                if (ivWed.isSelected()) {
                    ivWed.setSelected(false);
                } else {
                    ivWed.setSelected(true);
                }
                break;
            }
            case R.id.llThus: {
                if (ivThurs.isSelected()) {
                    ivThurs.setSelected(false);
                } else {
                    ivThurs.setSelected(true);
                }
                break;
            }
            case R.id.llFri: {
                if (ivFri.isSelected()) {
                    ivFri.setSelected(false);
                } else {
                    ivFri.setSelected(true);
                }
                break;
            }
            case R.id.llSat: {
                if (ivSat.isSelected()) {
                    ivSat.setSelected(false);
                } else {
                    ivSat.setSelected(true);
                }
                break;
            }
        }
    }
}

