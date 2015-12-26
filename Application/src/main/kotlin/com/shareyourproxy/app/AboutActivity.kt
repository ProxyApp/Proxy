package com.shareyourproxy.app

import android.os.Bundle
import android.view.MenuItem

import com.shareyourproxy.R
import com.shareyourproxy.app.fragment.AboutFragment

import timber.log.Timber

/**
 * Display an [AboutFragment] that has an Apache II license for this project.
 */
class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.activity_about_container,
                    AboutFragment.newInstance()).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> Timber.e("Menu Item ID unknown")
        }
        return false
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom)
    }
}
