package predawn.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "자료를 찾을 수 없습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다"),
    AUTHENTICATION_FAIL(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAIL", "인증에 실패 했습니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다"),
    PASSWORD_EMPTY(HttpStatus.BAD_REQUEST, "PASSWORD_EMPTY", "비밀번호는 빈 값일 수 없습니다"),
    ALREADY_REQUEST_FRIEND(HttpStatus.BAD_REQUEST, "ALREADY_REQUEST_FRIEND", "이미 친구 요청을 보낸 상태 입니다"),
    MAX_UPLOAD_SIZE_EXCEED(HttpStatus.PAYLOAD_TOO_LARGE, "MAX_UPLOAD_SIZE_EXCEED", "파일 크기가 너무 큽니다.\n업로드 가능한 최대 크기는 5MB입니다.\n파일을 줄이거나 압축하여 다시 시도해주세요."),
    NOT_ALLOWED_FILE(HttpStatus.BAD_REQUEST, "NOT_ALLOWED_FILE", "허용되지 않는 파일 입니다"),
    LIKE_UNIQUE_VIOLATION(HttpStatus.CONFLICT, "LIKE_UNIQUE_VIOLATION", "이미 좋아요를 누른 상태 입니다"),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "게시글을 찾을 수 없습니다"),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BOARD_ACCESS_DENIED", "게시글 접근 권한이 없습니다"),
    BOARD_CONFLICT(HttpStatus.FORBIDDEN, "BOARD_CONFLICT", "이미 수정된 게시글 입니다"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다"),
    FILE_NOT_SELECTED(HttpStatus.BAD_REQUEST, "FILE_NOT_SELECTED", "파일을 선택해 주세요"),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FILE_ACCESS_DENIED", "파일 접근 권한이 없습니다"),
    PAGE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PAGE_BAD_REQUEST", "잘못된 페이지 번호 입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
