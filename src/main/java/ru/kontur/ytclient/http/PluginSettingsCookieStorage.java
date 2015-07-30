package ru.kontur.ytclient.http;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.google.common.base.Joiner;

import java.util.List;

/**
 *
 * @author michael.plusnin
 */
public class PluginSettingsCookieStorage implements CookiesStorageInterface {
    private static final String COOKIES_KEY = "cookies";
    private static final Joiner JOINER = Joiner.on(';');

    private final PluginSettings pluginSettings;
    private final String         baseKey;

    public PluginSettingsCookieStorage(PluginSettings pluginSettings, String baseKey) {
        this.pluginSettings = pluginSettings;
        this.baseKey        = baseKey;
    }

    @Override
    public String getCookies() {
        Object stored;
        synchronized (pluginSettings) {
            stored = pluginSettings.get(baseKey + "." + COOKIES_KEY);
        }
        if (stored instanceof String)
            return (String)stored;
        return null;
    }

    @Override
    public void setCookies(String cookies) {
        synchronized (pluginSettings) {
            pluginSettings.put(baseKey + "." + COOKIES_KEY, cookies);
        }
    }

    @Override
    public void setCookies(List<String> cookies) {
        String joined = JOINER.join(cookies);
        synchronized (pluginSettings) {
            pluginSettings.put(baseKey + "." + COOKIES_KEY, joined);
        }
    }

    @Override
    public void erase() {
        synchronized (pluginSettings) {
            pluginSettings.remove(baseKey + "." + COOKIES_KEY);
        }
    }
}
