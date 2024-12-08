package com.wahid.everyDollar.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 500L;

    private final String error;
    private final HttpStatus httpStatus;

    public CustomException(String error, HttpStatus httpStatus) {
        super(error);
        this.error = error;
        this.httpStatus = httpStatus;
    }
}
