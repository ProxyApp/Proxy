package com.shareyourproxy.app

import android.os.Bundle
import android.support.v4.app.ActivityCompat

import com.shareyourproxy.app.fragment.SearchFragment

/**
 * Activity to handle displaying contacts and searching for new ones.
 */
internal final class SearchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preventStatusBarFlash(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content, SearchFragment()).commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAfterTransition(this)
    }
}
