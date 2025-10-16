package ch.afdanny.technicalexercise.clientcontractapi.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) { super(msg); }
}
