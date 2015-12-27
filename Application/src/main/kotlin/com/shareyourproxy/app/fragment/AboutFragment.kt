package com.shareyourproxy.app.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.R
import com.shareyourproxy.util.bindView

/**
 * Show an Apache II License for this project.
 */
class AboutFragment : BaseFragment() {
    private val toolbar: Toolbar by bindView(R.id.fragment_about_toolbar)

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_about, null, false)
        buildToolbar(toolbar, getString(R.string.about), null)
        return rootView
    }

    companion object {
        /**
         * Return a new instance of this fragment for the parent [AboutActivity].
         * @return AboutFragment
         */
        fun newInstance(): Fragment {
            return AboutFragment()
        }
    }

}
