package predawn.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;

    private ErrorResponse(HttpStatus httpStatus, String code, String message) {
        this.status = httpStatus.value();
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(HttpStatus httpStatus, String code, String message) {
        return new ErrorResponse(httpStatus, code, message);
    }
}
