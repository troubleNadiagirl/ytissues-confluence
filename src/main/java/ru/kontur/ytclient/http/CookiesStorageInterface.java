package ru.kontur.ytclient.http;

import java.util.List;

/**
 *
 * @author michael.plusnin
 */
public interface CookiesStorageInterface {
    String getCookies();
    void   setCookies(String cookies);
    void   setCookies(List<String> cookies);
    void   erase();
}
