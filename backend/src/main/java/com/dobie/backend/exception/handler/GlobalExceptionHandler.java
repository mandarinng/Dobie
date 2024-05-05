package com.dobie.backend.exception.handler;

import com.dobie.backend.exception.exception.Environment.BuildGradleNotFoundException;
import com.dobie.backend.exception.exception.Environment.FilePathNotExistException;
import com.dobie.backend.exception.exception.Environment.PackageJsonNotFoundException;
import com.dobie.backend.exception.exception.Environment.PomXmlNotFoundException;
import com.dobie.backend.exception.exception.git.GitCloneFailedException;
import com.dobie.backend.exception.format.code.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ApiResponse response;

    /* 경로 확인 */
    @ExceptionHandler(BuildGradleNotFoundException.class)
    protected ResponseEntity<?> handle(BuildGradleNotFoundException e) {
        log.error("BuildGradleNotFoundException = {}", e.getErrorCode().getMessage());
        return response.error(e.getErrorCode());
    }

    @ExceptionHandler(PomXmlNotFoundException.class)
    protected ResponseEntity<?> handle(PomXmlNotFoundException e) {
        log.error("PomXmlNotFoundException = {}", e.getErrorCode().getMessage());
        return response.error(e.getErrorCode());
    }
    @ExceptionHandler(PackageJsonNotFoundException.class)
    protected ResponseEntity<?> handle(PackageJsonNotFoundException e) {
        log.error("PackageJsonNotFoundException = {}", e.getErrorCode().getMessage());
        return response.error(e.getErrorCode());
    }

    @ExceptionHandler(FilePathNotExistException.class)
    protected ResponseEntity<?> handle(FilePathNotExistException e) {
        log.error("FilePathNotExistException = {}", e.getErrorCode().getMessage());
        return response.error(e.getErrorCode());
    }


    @ExceptionHandler(GitCloneFailedException.class)
    protected ResponseEntity<?> handle(GitCloneFailedException e) {
        log.error("GitCloneFailedException = {}", e.getErrorCode().getMessage());
        log.error("Error Message = {}", e.getErrorMessage());
        return response.error(e.getErrorCode());
    }
}
