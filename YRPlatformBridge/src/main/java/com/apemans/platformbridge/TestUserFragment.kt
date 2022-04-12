package com.apemans.platformbridge

import androidx.fragment.app.Fragment
import com.apemans.router.routerFragments
import com.apemans.xmessage.router.FRAGMENT_PATH_MESSAGE_LIST

object TestUserFragment {

    private val mineFragment by routerFragments("/user/account")

    fun getUserFragment() : Fragment {
        return mineFragment
    }
}