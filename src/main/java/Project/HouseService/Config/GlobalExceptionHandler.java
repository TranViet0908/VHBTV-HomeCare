// src/main/java/Project/HouseService/Config/GlobalExceptionHandler.java
package Project.HouseService.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MultipartException.class)
    public String handleMultipart(MultipartException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Ảnh tải lên không hợp lệ hoặc vượt giới hạn. Chọn ảnh <= 10MB.");
        return "redirect:/register";
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex, ServletWebRequest req) {
        log.error("Unhandled exception at {} {}", req.getRequest().getMethod(), req.getRequest().getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Đã có lỗi khi xử lý yêu cầu. Vui lòng thử lại.");
    }
}
