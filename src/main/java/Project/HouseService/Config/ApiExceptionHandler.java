package Project.HouseService.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> onError(Exception ex) {
        String msg = "Đã có lỗi khi xử lý yêu cầu. Vui lòng thử lại.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "<div class='p-3 text-red-700 bg-red-50 rounded'>" + msg + "</div>"
        );
    }
}
