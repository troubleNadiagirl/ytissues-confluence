package ru.kontur;

/**
 *
 * @author michael.plusnin
 */
public class ConnectionException extends Exception {
    public ConnectionException() {
        super();
    }

    public ConnectionException(String mess) {
        super(mess);
    }

    public ConnectionException(Throwable th) {
        super(th);
    }
}
