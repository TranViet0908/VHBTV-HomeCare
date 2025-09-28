package Project.HouseService.Gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AiGateway {

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    @Value("${ai.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;
    @Value("${ai.model:gemini-1.5-flash}")
    private String model;
    @Value("${ai.api-key:}")
    private String apiKey;

    private static String trimSlash(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private static String toJson(String s) throws Exception {
        return new ObjectMapper().writeValueAsString(s);
    }

    public String ask(String systemPrompt, String userQuestion) {
        try {
            if (apiKey == null || apiKey.isBlank())
                throw new IllegalStateException("GEMINI_API_KEY chưa được cấu hình");

            String path = "/v1beta/models/" + model + ":generateContent";
            String url = trimSlash(baseUrl) + path + "?key=" +
                    URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String prompt = systemPrompt + "\nCâu hỏi: " + userQuestion;
            String body = """
                      { "contents": [ { "role":"user", "parts":[ { "text": %s } ] } ],
                        "generationConfig": { "temperature": 0.3 } }
                    """.formatted(toJson(prompt));

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300)
                throw new RuntimeException("Gemini HTTP " + resp.statusCode() + ": " + resp.body());

            JsonNode root = om.readTree(resp.body());
            JsonNode cand = root.path("candidates");
            if (cand.isArray() && cand.size() > 0) {
                JsonNode parts = cand.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode t = parts.get(0).get("text");
                    if (t != null) return t.asText();
                }
            }
            return "";
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
