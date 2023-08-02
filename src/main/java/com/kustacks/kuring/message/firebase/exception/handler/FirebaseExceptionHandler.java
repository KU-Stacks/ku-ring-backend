package com.kustacks.kuring.message.firebase.exception.handler;

import com.kustacks.kuring.common.dto.ErrorResponse;
import com.kustacks.kuring.message.firebase.exception.FirebaseInvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FirebaseExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> FirebaseInvalidTokenException(FirebaseInvalidTokenException exception) {
        log.error("[FirebaseInvalidTokenException] {}", exception.getMessage());

        return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(exception.getErrorCode()));
    }
}
