package predawn.web.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final int status;
    private final T value;

    public ApiResponse(int status, T value) {
        this.status = status;
        this.value = value;
    }
}
