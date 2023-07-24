package com.foodstagram.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResult> fileSizeHandler(MaxUploadSizeExceededException e) {
        log.info("파일 용량이 초과되었습니다.");
        ErrorResult errorResult = new ErrorResult("file too large", "파일 용량이 초과되었습니다.");
        return new ResponseEntity<>(errorResult, HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }
}
