package ru.kontur.settings;

/**
 *
 * @author michael.plusnin
 */
public interface YtConnectionSettingsStorageInterface {
    String getBaseUrl();
    void   setBaseUrl(String newUrl);

    String getUsername();
    void   setUsername(String newUsername);

    String getPassword();
    void   setPassword(String newPassword);
}
