package com.afar.osaio.smart.electrician.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.presenter.FamilyMemberPresenter;
import com.afar.osaio.smart.electrician.presenter.IFamilyMemberPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IFamilyMemberView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * FamilyMemberActivity
 *
 * @author Administrator
 * @date 2019/3/15
 */
public class FamilyMemberActivity extends BaseActivity implements IFamilyMemberView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivPortrait)
    ImageView ivPortrait;
    @BindView(R.id.tvNickName)
    TextView tvNickName;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.tvMemberAccess)
    TextView tvMemberAccess;
    @BindView(R.id.ivNameArrow)
    ImageView ivNameArrow;
    @BindView(R.id.btnMemberRemove)
    FButton btnMemberRemove;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IFamilyMemberPresenter mPresenter;

    private String account;
    private String access;
    private long memberId;
    private int mRole;
    private int mMineRole;
    private String mNickName;
    private String mUrl;

    public static void toFamilyMemberActivity(Activity from, int requestCode, long memberId,String account,int role,
                                              int mineRole,String nickName,String url) {
        Intent intent = new Intent(from, FamilyMemberActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT,account);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ROLE, role);
        intent.putExtra(ConstantValue.INTENT_KEY_MINE_ROLE, mineRole);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, nickName);
        intent.putExtra(ConstantValue.INTENT_KEY_URL, url);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_member);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.my_profile);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mPresenter = new FamilyMemberPresenter(this);
            memberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
            account = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT);
            mRole = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_MEMBER_ROLE, 0);
            mMineRole = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_MINE_ROLE, 0);
            mNickName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME);
            mUrl = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_URL);

            initUi();
            if(!TextUtils.isEmpty(account)){
                mPresenter.getAccount(account);
            }
        }
    }

    private void initUi(){
        if (mRole == -1 || mRole == 0 ){
            ivNameArrow.setVisibility(View.INVISIBLE);
            access = getResources().getString(R.string.member);
        }else if (mRole == 1 || mRole == 2){
            ivNameArrow.setVisibility(View.VISIBLE);
            access = getResources().getString(R.string.owner);
        }
        tvMemberAccess.setText(access);
        tvNickName.setText(mNickName);

        if (mMineRole == 2){
            btnMemberRemove.setVisibility(View.VISIBLE);
        }else {
            btnMemberRemove.setVisibility(View.GONE);
        }
        Glide.with(NooieApplication.mCtx)
                .load(mUrl)
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                .into(ivPortrait);

    }

    @SuppressLint("StringFormatInvalid")
    @OnClick({R.id.ivLeft, R.id.containerNickname, R.id.btnMemberRemove})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                setResult(RESULT_OK, new Intent());
                finish();
                break;
            }
            case R.id.containerNickname: {
                if (mRole == 0 || mRole == 1 || mRole == 2){
                    SetNameActivity.toSetNameActivity(this, ConstantValue.REQUEST_CODE_MEMBER_RENAME, tvNickName.getText().toString(), memberId);
                }
                break;
            }

            case R.id.btnMemberRemove: {
                DialogUtil.showConfirmWithSubMsgDialog(this, getString(R.string.remove_access),
                        String.format(getResources().getString(R.string.remove_access_tip), tvNickName.getText().toString().trim(), access.toLowerCase()), R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                            @Override
                            public void onClickRight() {
                                long memberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
                                if (memberId != 0) {
                                    showLoadingDialog();
                                    mPresenter.removeMember(memberId);
                                }
                            }
                            @Override
                            public void onClickLeft() {
                            }
                        });
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_DEVICE_REMOVE: {
                    mPresenter.loadUserShareInfo(memberId);
                    break;
                }
                case ConstantValue.REQUEST_CODE_MEMBER_RENAME: {
                    mPresenter.loadUserShareInfo(memberId);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyRemoveMemberState(String msg) {
        hideLoadingDialog();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this,msg);
        }
    }

    @Override
    public void notifyLoadUserShareInfoSuccess(ShareSentUserDetailBean detailBean) {
        if (detailBean != null && detailBean.getDevices() != null) {
            if (detailBean.getRemarkName() != null && detailBean.getRemarkName().length() > 0) {
                tvNickName.setText(detailBean.getRemarkName());
            } else if (detailBean.getNameWithoutRemark() != null) {
                tvNickName.setText(detailBean.getNameWithoutRemark());
            }
        }
    }

    @Override
    public void notifyLoadUserShareInfoFailed(String msg) {
    }

    @Override
    public void notifyGetAccountSuccess(String account) {
        tvAccount.setText(account);
    }

    @Override
    public void notifyGetAccountFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

}
