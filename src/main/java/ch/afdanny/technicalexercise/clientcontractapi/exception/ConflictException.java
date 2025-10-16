package ch.afdanny.technicalexercise.clientcontractapi.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String msg, Throwable cause) { super(msg, cause); }
}
