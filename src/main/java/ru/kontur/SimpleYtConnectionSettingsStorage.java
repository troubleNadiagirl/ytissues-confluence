package ru.kontur;

/**
 * This class is thread safe, all instances has some values
 * @author michael.plusnin
 */
public class SimpleYtConnectionSettingsStorage
        implements YtConnectionSettingsStorageInterface{
    private static String baseUrl;
    private static final String BASE_URL_SYNC = "BASEURLSYNC";
    private static String username;
    private static final String USERNAME_SYNC = "USERNAMESYNC";
    private static String password;
    private static final String PASSWORD_SYNC = "PASSWORDSYNC";

    /**
     * Nothing slashes at the last positions of result
     */
    @Override
    public String getBaseUrl() {
        synchronized (BASE_URL_SYNC) {
            return baseUrl;
        }
    }

    /**
     * This method erase last slashes
     */
    @Override
    public void setBaseUrl(String newUrl) {
        String nextUrl = IssueUrlComposer.removeLastSlashes(newUrl);
        synchronized (BASE_URL_SYNC) {
            baseUrl = nextUrl;
        }
    }

    @Override
    public String getUsername() {
        synchronized (USERNAME_SYNC) {
            return username;
        }
    }

    @Override
    public void setUsername(String newUsername) {
        synchronized (USERNAME_SYNC) {
            username = newUsername;
        }
    }

    @Override
    public String getPassword() {
        synchronized (PASSWORD_SYNC) {
            return password;
        }
    }

    @Override
    public void setPassword(String newPassword) {
        synchronized (PASSWORD_SYNC) {
            password = newPassword;
        }
    }
}
