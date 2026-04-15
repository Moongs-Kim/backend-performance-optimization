package predawn.global.error.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ViewExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException e, Model model) {
        log.info("ViewExceptionHandler Error Message: {}", e.getMessage(), e);

        ErrorCode code = e.getErrorCode();
        HttpStatus status = code.getStatus();

        model.addAttribute("status", status.value());
        model.addAttribute("message", code.getMessage());

        return (status == HttpStatus.NOT_FOUND) ? "error/404" : "error/4xx";
    }

}
