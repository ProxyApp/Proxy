package com.shareyourproxy.app

import android.os.Bundle

import com.shareyourproxy.app.fragment.MainIntroductionFragment

/**
 * Introduce a user with a view pager flow.
 */
internal final class IntroductionActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content, MainIntroductionFragment()).commit()
        }
    }
}
