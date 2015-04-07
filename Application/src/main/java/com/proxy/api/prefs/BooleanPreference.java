package com.proxy.api.prefs;

import android.content.SharedPreferences;

/**
 * Turn a boolean value {@link SharedPreferences} into an easy to use wrapper object.
 */
@SuppressWarnings("unused")
public class BooleanPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final boolean defaultValue;

    /**
     * Constructor.
     *
     * @param preferences {@link SharedPreferences}
     * @param key         shared preference key value
     */
    public BooleanPreference(SharedPreferences preferences, String key) {
        this(preferences, key, false);
    }

    /**
     * Constructor.
     *
     * @param preferences {@link SharedPreferences}
     * @param key shared preference key value
     * @param defaultValue default preference value
     */
    public BooleanPreference(SharedPreferences preferences, String key, boolean defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    /**
     * Getter
     * @return {@link SharedPreferences} preference value
     */
    public boolean get() {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * Has the {@link SharedPreferences} key been added.
     * @return is shared preference key present
     */
    public boolean isSet() {
        return preferences.contains(key);
    }

    /**
     * Setter.
     * @param value boolean preference value
     */
    public void set(boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }


    /**
     * Remove key from {@link SharedPreferences} table.
     */
    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
