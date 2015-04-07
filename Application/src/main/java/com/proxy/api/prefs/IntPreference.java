package com.proxy.api.prefs;

import android.content.SharedPreferences;

/**
 * Turn an integer value {@link SharedPreferences} into an easy to use wrapper object.
 */
@SuppressWarnings("unused")
public class IntPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final int defaultValue;

    /**
     * Constructor.
     *
     * @param preferences {@link SharedPreferences}
     * @param key         preference key value
     */
    public IntPreference(SharedPreferences preferences, String key) {
        this(preferences, key, 0);
    }

    /**
     * Constructor.
     *
     * @param preferences  {@link SharedPreferences}
     * @param key          preference key value
     * @param defaultValue default preference value
     */
    public IntPreference(SharedPreferences preferences, String key, int defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    /**
     * Getter.
     *
     * @return {@link SharedPreferences}
     */
    public int get() {
        return preferences.getInt(key, defaultValue);
    }

    /**
     * Has the {@link SharedPreferences} key been added.
     *
     * @return is shared preference key present
     */
    public boolean isSet() {
        return preferences.contains(key);
    }

    /**
     * Setter.
     *
     * @param value set integer value
     */
    public void set(int value) {
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * Remove key from {@link SharedPreferences} table.
     */
    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
