package com.afar.osaio.testrn.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.afar.osaio.R
import com.afar.osaio.testrn.widget.YRRoundProgress

/**
 * 自定义圆形dialog
 */
class YRCustomProgressDialog(context : Context) {
    lateinit var progressYR : YRRoundProgress

    init {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val v: View = inflater.inflate(R.layout.rn_loading_dialog, null) // 得到加载view
        progressYR = v.findViewById<YRRoundProgress>(R.id.roundProgress)
    }
    /**
     * 得到自定义的progressDialog
     * @param context
     * @param msg
     * @return
     */
    fun createLoadingDialog(context: Context?, msg: String?): Dialog? {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val v: View = inflater.inflate(R.layout.rn_loading_dialog, null) // 得到加载view
        val layout: LinearLayout = v.findViewById(R.id.dialog_view) as LinearLayout // 加载布局
        // main.xml中的ImageView
//        val spaceshipImage: ImageView = v.findViewById(R.id.img) as ImageView
//        val tipTextView: TextView = v.findViewById(R.id.tipTextView) as TextView // 提示文字
//        // 加载动画
//        val hyperspaceJumpAnimation: Animation = AnimationUtils.loadAnimation(
//            context, R.anim.load_animation)
//        // 使用ImageView显示动画
//        spaceshipImage.startAnimation(hyperspaceJumpAnimation)
//        tipTextView.setText(msg) // 设置加载信息
        progressYR = v.findViewById<YRRoundProgress>(R.id.roundProgress)
        val loadingDialog = context?.let { Dialog(it, R.style.loading_dialog) } // 创建自定义样式dialog
        loadingDialog?.setCancelable(false) // 不可以用“返回键”取消
        loadingDialog?.setContentView(
            layout, ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        ) // 设置布局
        return loadingDialog
    }

    fun setProgress(process: Int) {
        progressYR.progress = process
    }
}