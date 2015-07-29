package ru.kontur;

import com.google.common.base.Joiner;
import java.util.List;

/**
 * This class is thread safe
 * Settings is stored in this class
 * @author michael.plusnin
 */
public class SimpleCookiesStorage implements CookiesStorageInterface {
    private String cookies;
    private final String SYNC_COOKIES_OBJECT = "SYNC";
    private static final Joiner JOINER = Joiner.on(';');

    @Override
    public String getCookies() {
        synchronized (SYNC_COOKIES_OBJECT) {
            return cookies;
        }
    }

    @Override
    public void setCookies(String cookies) {
        synchronized (SYNC_COOKIES_OBJECT) {
            this.cookies = cookies;
        }
    }

    @Override
    public void setCookies(List<String> cookies) {
        synchronized (SYNC_COOKIES_OBJECT) {
            this.cookies = JOINER.join(cookies);
        }
    }

    @Override
    public void erase() {
        synchronized (SYNC_COOKIES_OBJECT) {
            this.cookies = null;
        }
    }
}
