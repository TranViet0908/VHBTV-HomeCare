// src/main/java/Project/HouseService/Service/FileStorageService.java
package Project.HouseService.Service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Path.of("uploads");
    private final Path avatarDir = root.resolve("avatars");

    public FileStorageService() {
        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            throw new RuntimeException("Không tạo được thư mục uploads", e);
        }
    }

    public String storeAvatar(MultipartFile file) {
        try {
            if (file.isEmpty()) throw new IllegalArgumentException("File rỗng");
            String ext = getExt(StringUtils.cleanPath(file.getOriginalFilename()));
            String name = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
            Path target = avatarDir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            // Trả về URL public, phần map tĩnh xem WebConfig
            return "/uploads/avatars/" + name;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage(), e);
        }
    }

    private String getExt(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }
}
