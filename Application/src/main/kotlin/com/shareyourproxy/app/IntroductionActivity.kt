package com.shareyourproxy.app

import android.os.Bundle

import com.shareyourproxy.app.fragment.MainIntroductionFragment

/**
 * Introduce a user with a view pager flow.
 */
class IntroductionActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val mainFragment = MainIntroductionFragment.newInstance()
            supportFragmentManager.beginTransaction().replace(android.R.id.content, mainFragment).commit()
        }
    }
}
