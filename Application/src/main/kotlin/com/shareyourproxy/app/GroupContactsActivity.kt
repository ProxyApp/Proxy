package com.shareyourproxy.app

import android.os.Bundle
import android.view.MenuItem

import com.shareyourproxy.R
import com.shareyourproxy.app.fragment.GroupContactsFragment

import timber.log.Timber

/**
 * Activity to display the contacts that a user has saved in a selected group.
 */
class GroupContactsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content,
                    GroupContactsFragment.newInstance()).commit()
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
