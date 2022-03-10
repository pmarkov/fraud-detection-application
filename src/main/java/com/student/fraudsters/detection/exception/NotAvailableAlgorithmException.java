package com.student.fraudsters.detection.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotAvailableAlgorithmException extends RuntimeException {

    public NotAvailableAlgorithmException(String algorithmId) {
        super(String.format("The algorithm %s is not available.", algorithmId));
    }
}
