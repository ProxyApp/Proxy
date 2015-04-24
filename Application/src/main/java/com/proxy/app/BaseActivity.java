package com.proxy.app;

import com.proxy.ProxyApplication;
import com.proxy.api.model.User;

/**
 * Base abstraction for all Activities to inherit from.
 */
public class BaseActivity extends android.support.v7.app.ActionBarActivity {

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public User getCurrentUser() {
        return ((ProxyApplication) getApplication()).getCurrentUser();
    }

    /**
     * Set the currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @param user currently logged in
     */
    public void setCurrentUser(User user) {
        ((ProxyApplication) getApplication()).setCurrentUser(user);
    }

}
