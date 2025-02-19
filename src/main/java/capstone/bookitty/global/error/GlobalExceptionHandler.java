package capstone.bookitty.global.error;


import capstone.bookitty.global.error.exception.BusinessException;
import capstone.bookitty.global.error.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleEntityNotFoundException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class) //잘못된 인자 전달
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e){
        log.warn("IllegalArgumentException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus()));
    }

    @ExceptionHandler(RuntimeException.class) //서버 내부 오류
    public ResponseEntity<ErrorResponse> handleRunTimeException(RuntimeException e){
        log.warn("RuntimeException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()));
    }

    @ExceptionHandler(BusinessException.class) //요청한 리소스나 엔티티 찾을 수 없음
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(final BusinessException e){
        log.warn("EntityNotFoundException: {}", e.getMessage());
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
    }

    @ExceptionHandler(AccessDeniedException.class) //권한 부족
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(final AccessDeniedException e){
        log.warn("AccessDeniedException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.HANDLE_ACCESS_DENIED);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.HANDLE_ACCESS_DENIED.getStatus()));
    }

    @ExceptionHandler(MultipartException.class) //multipartfile 처리
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException e) {
        log.warn("MultipartException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.MULTIPART_INVALID);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.MULTIPART_INVALID.getStatus()));
    }

}
