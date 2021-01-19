package com.piotrak.kalah.controller.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorResponse {

  private final String message;
}
