package ru.kontur.ytclient;

/**
 *
 * @author michael.plusnin
 */
public class ParseException extends Exception {
    public ParseException() {
        super();
    }

    public ParseException(String mess) {
        super(mess);
    }

    public ParseException(Throwable th) {
        super(th);
    }
}
