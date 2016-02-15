package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.R
import com.shareyourproxy.R.layout.fragment_about
import com.shareyourproxy.R.string.about
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView

/**
 * Show an Apache II License for this project.
 */
internal final class AboutFragment() : BaseFragment() {
    private val toolbar: Toolbar by bindView(R.id.fragment_about_toolbar)
    private val title: String by bindString(about)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(fragment_about, null, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buildToolbar(toolbar, title)
    }
}
