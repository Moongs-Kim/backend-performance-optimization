package predawn.global.error.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import predawn.global.error.ErrorCode;
import predawn.global.error.ErrorResponse;
import predawn.global.error.FieldErrorResponse;
import predawn.global.error.ValidationErrorResponse;
import predawn.global.error.exception.BusinessException;
import predawn.global.error.exception.ValidationException;

import java.util.List;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        log.info("ApiExceptionHandler Error Message: {}", e.getMessage(), e);

        ErrorCode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.of(
                        code.getStatus(),
                        code.getCode(),
                        code.getMessage())
                );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(ValidationException e) {
        log.info("ApiExceptionHandler Error Message: {}", e.getMessage(), e);

        List<FieldErrorResponse> fieldErrorResponses =
                e.getBindingResult().getFieldErrors()
                        .stream()
                        .map(fieldError -> FieldErrorResponse.of(fieldError.getField(), fieldError.getDefaultMessage()))
                        .toList();

        return ResponseEntity
                .badRequest()
                .body(ValidationErrorResponse.of(fieldErrorResponses));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariable(MissingPathVariableException e) {
        log.info("ApiExceptionHandler Error Message: {}", e.getMessage(), e);

        ErrorCode code = ErrorCode.NOT_FOUND;
        HttpStatus badRequest = code.getStatus();

        return ResponseEntity
                .status(badRequest)
                .body(ErrorResponse.of(
                        badRequest,
                        code.getCode(),
                        code.getMessage()
                ));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingMultipartFile(MissingServletRequestPartException e) {
        log.info("ApiExceptionHandler Error Message: {}", e.getMessage(), e);

        return responseBadRequest();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.info("ApiExceptionHandler Error Message: {}", e.getMessage(), e);

        return responseBadRequest();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceed(MaxUploadSizeExceededException e) {
        log.info("ApiExceptionHandler Error Message: {}", e.getMessage(), e);

        ErrorCode code = ErrorCode.MAX_UPLOAD_SIZE_EXCEED;
        HttpStatus payloadTooLarge = code.getStatus();

        return ResponseEntity
                .status(payloadTooLarge)
                .body(ErrorResponse.of(
                        payloadTooLarge,
                        code.getCode(),
                        code.getMessage()
                ));
    }

    private ResponseEntity<ErrorResponse> responseBadRequest() {
        ErrorCode code = ErrorCode.BAD_REQUEST;
        HttpStatus badRequest = code.getStatus();

        return ResponseEntity
                .status(badRequest)
                .body(ErrorResponse.of(
                        badRequest,
                        code.getCode(),
                        code.getMessage()
                ));
    }

}
