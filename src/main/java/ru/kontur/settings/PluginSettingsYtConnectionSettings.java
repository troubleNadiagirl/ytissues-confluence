package ru.kontur.settings;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import ru.kontur.IssueUrlComposer;

/**
 *
 * @author michael.plusnin
 */
public class PluginSettingsYtConnectionSettings
        implements YtConnectionSettingsStorageInterface{
    private static final String URL_KEY = "yturl";
    private static final String USERNAME_KEY = "ytusername";
    private static final String PASSWORD_KEY = "ytpassword";

    private final String baseKey;
    private PluginSettings pluginSettings;

    public PluginSettingsYtConnectionSettings(PluginSettings pluginSettings, String baseKey) {
        this.pluginSettings = pluginSettings;
        this.baseKey        = baseKey;
    }

    private String get(String key) {
        Object stored;
        synchronized (pluginSettings) {
            stored = pluginSettings.get(baseKey + "." + key);
        }
        String result = null;
        if (stored instanceof String)
            result = (String)stored;
        return result;
    }

    private void set(String key, String value) {
        synchronized (pluginSettings) {
            pluginSettings.put(baseKey + "." + key, value);
        }
    }

    @Override
    public String getBaseUrl() {
        return get(URL_KEY);
    }

    @Override
    public void setBaseUrl(String newUrl) {
        String nextUrl = IssueUrlComposer.removeLastSlashes(newUrl);
        set(URL_KEY, nextUrl);
    }

    @Override
    public String getUsername() {
        return get(USERNAME_KEY);
    }

    @Override
    public void setUsername(String newUsername) {
        set(USERNAME_KEY, newUsername);
    }

    @Override
    public String getPassword() {
        return get(PASSWORD_KEY);
    }

    @Override
    public void setPassword(String newPassword) {
        set(PASSWORD_KEY, newPassword);
    }
}
