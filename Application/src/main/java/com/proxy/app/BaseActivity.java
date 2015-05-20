package com.proxy.app;

import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.proxy.ProxyApplication;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.RxBusDriver;

/**
 * Base abstraction for all Activities to inherit from.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public User getLoggedInUser() {
        return ((ProxyApplication) getApplication()).getCurrentUser();
    }

    /**
     * Set the currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @param user currently logged in
     */
    public void setLoggedInUser(User user) {
        ((ProxyApplication) getApplication()).setCurrentUser(user);
    }

    public boolean isLoggedInUser(User user){
        return user.id().equals(getLoggedInUser().id());
    }

    public RxBusDriver getRxBus(){
        return ((ProxyApplication) getApplication()).getRxBus();
    }

    protected void buildToolbar(Toolbar toolbar, String title, Drawable icon) {
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(title);
        bar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(icon);
    }

}
