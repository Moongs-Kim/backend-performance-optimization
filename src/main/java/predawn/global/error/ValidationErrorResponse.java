package predawn.global.error;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationErrorResponse {

    private final String code = "VALIDATION_ERROR";
    private final String message = "Validation Failed";
    private final int status = 400;
    private final List<FieldErrorResponse> errors;

    private ValidationErrorResponse(List<FieldErrorResponse> errors) {
        this.errors = errors;
    }

    public static ValidationErrorResponse of(List<FieldErrorResponse> errors) {
        return new ValidationErrorResponse(errors);
    }
}
