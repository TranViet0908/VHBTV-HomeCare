// src/main/java/Project/HouseService/Gateway/AiGateway.java
package Project.HouseService.Gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AiGateway {

    private final RestClient http;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${ai.base-url}")
    private String baseUrl;
    @Value("${ai.api-version}")
    private String apiVersion;
    @Value("${ai.model}")
    private String model;
    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${company.name}")
    private String companyName;
    @Value("${chat.agent-name}")
    private String agentName;
    @Value("${chat.system-rules}")
    private String systemRules;
    @Value("${chat.greeting-template}")
    private String greetingTpl;

    // cấu hình HTTP với timeout
    @Value("${ai.http.connect-timeout-ms:5000}")
    private int connectTimeoutMs;
    @Value("${ai.http.read-timeout-ms:20000}")
    private int readTimeoutMs;
    @Value("${ai.http.max-retries:3}")
    private int maxRetries;

    public AiGateway() {
        var jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .build();
        var factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofMillis(20000));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    // Khởi tạo lại RestClient khi có cấu hình timeout custom
    private RestClient client() {
        var jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, connectTimeoutMs)))
                .build();
        var factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofMillis(Math.max(2000, readTimeoutMs)));
        return RestClient.builder().requestFactory(factory).build();
    }

    public String greeting() {
        return String.format(greetingTpl, agentName, companyName);
    }

    /**
     * userPrompt: câu hỏi hiện tại
     * historyPairs: danh sách [user, model] gần nhất; sẽ cắt còn tối đa 5 cặp
     */
    public String generate(String userPrompt, List<String[]> historyPairs) {
        String preface = """
            Vai trò: %s — trợ lý CSKH của %s.
            Quy tắc: %s
            Ngữ điệu: ngắn gọn, lịch sự, tiếng Việt.
            Ngữ cảnh: Sàn trung gian kết nối Customer ↔ Vendor. Chỉ tư vấn, không thao tác tài khoản, không đặt lịch, chỉ hướng dẫn cách tự tra cứu.
            """.formatted(agentName, companyName, systemRules);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(msg("user", preface));

        if (historyPairs != null && !historyPairs.isEmpty()) {
            int start = Math.max(0, historyPairs.size() - 5); // tối đa 5 cặp
            for (int i = start; i < historyPairs.size(); i++) {
                String[] pair = historyPairs.get(i);
                if (pair[0] != null && !pair[0].isBlank()) contents.add(msg("user", pair[0]));
                if (pair[1] != null && !pair[1].isBlank()) contents.add(msg("model", pair[1]));
            }
        }
        contents.add(msg("user", userPrompt));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        Map<String, Object> gen = new HashMap<>();
        gen.put("temperature", 0.3);
        gen.put("maxOutputTokens", 1024);
        body.put("generationConfig", gen);

        String url = "%s/%s/models/%s:generateContent?key=%s"
                .formatted(baseUrl, apiVersion, model, apiKey);

        String resp = postWithRetry(url, body, Math.max(1, maxRetries));
        if (resp == null) return fallbackAnswer();

        try {
            JsonNode root = om.readTree(resp);
            // Kiểm tra finishReason nếu có
            String finishReason = root.path("candidates").path(0).path("finishReason").asText("");
            if ("SAFETY".equalsIgnoreCase(finishReason) || "BLOCKED".equalsIgnoreCase(finishReason)) {
                return safetyBlocked();
            }
            // Ghép toàn bộ parts.text
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode p : parts) {
                    String t = p.path("text").asText("");
                    if (!t.isBlank()) sb.append(t);
                }
                String out = sb.toString().trim();
                if (!out.isEmpty()) return out;
            }
        } catch (Exception e) {
            System.err.println("Gemini parse error: " + e.getMessage());
        }
        return fallbackAnswer();
    }

    private String postWithRetry(String url, Object body, int maxTries) {
        RestClient cli = client();
        for (int attempt = 1; attempt <= maxTries; attempt++) {
            try {
                return cli.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            } catch (RestClientResponseException ex) {
                int code = ex.getRawStatusCode();
                boolean retryable = code == 429 || code == 500 || code == 502 || code == 503 || code == 504;
                System.err.println("Gemini HTTP " + code + " attempt " + attempt + ": " + ex.getResponseBodyAsString());
                if (!retryable || attempt == maxTries) return null;
            } catch (RestClientException ex) {
                System.err.println("Gemini network err attempt " + attempt + ": " + ex.getMessage());
                if (attempt == maxTries) return null;
            }
            // Exponential backoff + jitter, tối đa 5s
            long base = 400L << (attempt - 1); // 400, 800, 1600, ...
            long sleep = base + ThreadLocalRandom.current().nextLong(base);
            try { Thread.sleep(Math.min(sleep, 5000)); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        return null;
    }

    private Map<String, Object> msg(String role, String text) {
        return Map.of("role", role, "parts", List.of(Map.of("text", text)));
    }

    private String fallbackAnswer() {
        return "Hiện Sana chưa kết nối được máy chủ AI.\n"
                + "- Bạn có thể tự tra cứu: Đăng nhập → Đơn hàng của tôi.\n"
                + "- Câu hỏi về dịch vụ, giá tham khảo mình vẫn có thể hướng dẫn chung.";
    }

    private String safetyBlocked() {
        return "Xin lỗi, yêu cầu này không phù hợp chính sách an toàn. "
                + "Bạn có thể hỏi về thông tin dịch vụ, giá tham khảo hoặc cách tự tra cứu đơn.";
    }
    public List<String> listModels() {
        try {
            String url = "%s/%s/models?key=%s".formatted(baseUrl, apiVersion, apiKey);
            String resp = client().get().uri(url).retrieve().body(String.class);
            JsonNode arr = om.readTree(resp).path("models");
            List<String> ok = new ArrayList<>();
            for (JsonNode m : arr) {
                boolean can = false;
                for (JsonNode g : m.path("supportedGenerationMethods")) {
                    if ("generateContent".equals(g.asText())) { can = true; break; }
                }
                if (can) ok.add(m.path("name").asText());
            }
            System.out.println("Gemini models available: " + ok);
            return ok;
        } catch (Exception e) { System.err.println("ListModels error: " + e.getMessage()); return List.of(); }
    }
}
