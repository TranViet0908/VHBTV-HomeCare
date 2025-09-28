// src/main/java/Project/HouseService/Config/GlobalExceptionHandler.java
package Project.HouseService.Config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {
    @ExceptionHandler(MultipartException.class)
    public String handleMultipart(MultipartException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Ảnh tải lên không hợp lệ hoặc vượt giới hạn. Chọn ảnh <= 10MB.");
        return "redirect:/register";
    }
}
