package com.piotrak.kalah.exception.handler;

import com.piotrak.kalah.controller.response.ErrorResponse;
import com.piotrak.kalah.exception.ForbiddenOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {ForbiddenOperationException.class, IllegalArgumentException.class})
  protected ResponseEntity<Object> handleException(RuntimeException ex, WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
      .message(ex.getMessage())
      .build();

    return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST,
      request);
  }

}
