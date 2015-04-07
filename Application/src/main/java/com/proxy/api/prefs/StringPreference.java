package com.proxy.api.prefs;

import android.content.SharedPreferences;

/**
 * Turn a string value {@link SharedPreferences} into an easy to use wrapper object.
 */
@SuppressWarnings("unused")
public class StringPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final String defaultValue;

    /**
     * Constructor.
     *
     * @param preferences {@link SharedPreferences}
     * @param key         shared preference key value
     */
    public StringPreference(SharedPreferences preferences, String key) {
        this(preferences, key, null);
    }

    /**
     * Constructor.
     *
     * @param preferences  {@link SharedPreferences}
     * @param key          shared preference key value
     * @param defaultValue default preference value
     */
    public StringPreference(SharedPreferences preferences, String key, String defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    /**
     * Getter.
     *
     * @return get preference value
     */
    public String get() {
        return preferences.getString(key, defaultValue);
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
     * @param value string preference value
     */
    public void set(String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Remove key from {@link SharedPreferences} table.
     */
    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
