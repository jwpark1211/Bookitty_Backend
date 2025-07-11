package capstone.bookitty.global.error;


import capstone.bookitty.domain.member.exception.*;
import capstone.bookitty.global.error.exception.BusinessException;
import capstone.bookitty.global.error.exception.EntityNotFoundException;
import capstone.bookitty.global.error.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MultipartException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE,e.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.warn("HandlerMethodValidationException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus()));
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

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(final EntityNotFoundException e){
        log.warn("EntityNotFoundException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.ENTITY_NOT_FOUND,e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.ENTITY_NOT_FOUND.getStatus()));
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

    @ExceptionHandler(DuplicateEmailException.class) //이메일 중복
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("DuplicateEmailException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.EMAIL_DUPLICATION);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.EMAIL_DUPLICATION.getStatus()));
    }

    @ExceptionHandler(RefreshTokenSaveException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenSaveException(RefreshTokenSaveException e) {
        log.warn("RefreshTokenSaveException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REFRESH_TOKEN_SAVE_INVALID);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.REFRESH_TOKEN_SAVE_INVALID.getStatus()));
    }

    @ExceptionHandler(UnauthenticatedMemberException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticatedMemberException(UnauthenticatedMemberException e) {
        log.warn("UnauthenticatedMemberException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.UNAUTHENTICATED_MEMBER);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.UNAUTHENTICATED_MEMBER.getStatus()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        log.warn("InvalidRefreshTokenException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REFRESH_TOKEN);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.INVALID_REFRESH_TOKEN.getStatus()));
    }

    @ExceptionHandler(NotLoggedInException.class)
    public ResponseEntity<ErrorResponse> handleNotLoggedInException(NotLoggedInException e) {
        log.warn("NotLoggedInException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_LOGGED_IN);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.NOT_LOGGED_IN.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleEntityNotFoundException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
