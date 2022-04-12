package com.apemans.platformbridge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.apemans.router.routerFragments
import com.apemans.xmessage.router.FRAGMENT_PATH_MESSAGE_LIST
import com.dylanc.longan.startActivity

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/8 11:43 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
class TestMessageActivity : AppCompatActivity() {

    private val messageFragment by routerFragments(FRAGMENT_PATH_MESSAGE_LIST)
    private val mineFragment by routerFragments("/user/account")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_message)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, mineFragment/*messageFragment*/)
            }
        }
    }

    companion object {
        fun start() {
            startActivity<TestMessageActivity>()
        }
    }
}