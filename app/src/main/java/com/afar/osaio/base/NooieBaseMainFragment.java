package com.afar.osaio.base;

import com.afar.osaio.R;
import com.afar.osaio.util.ToastUtil;

/**
 * 懒加载
 * Created by YoKeyword on 16/6/5.
 */
public abstract class NooieBaseMainFragment extends NooieBaseSupportFragment {
    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long TOUCH_TIME = 0;

    /**
     * 处理回退事件
     *
     * @return
     */
    @Override
    public boolean onBackPressedSupport() {
        if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
            _mActivity.finish();
        } else {
            TOUCH_TIME = System.currentTimeMillis();
            ToastUtil.showToast(getActivity(), R.string.exit_app_tip);
        }
        return true;
    }
}
