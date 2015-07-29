package ru.kontur;

/**
 *
 * @author michael.plusnin
 */
public class UnableLoginException extends ConnectionException {
    public UnableLoginException() {
        super();
    }

    public UnableLoginException(String mess) {
        super(mess);
    }

    public UnableLoginException(Throwable th) {
        super(th);
    }
}
