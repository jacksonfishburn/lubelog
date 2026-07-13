package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import dev.jacksonfishburn.lubelog.client.GeminiClient;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

/**
 * Opt-in smoke test against the real Gemini API.
 *
 * <p>Skipped unless {@code GEMINI_LIVE_TEST=true}. Resolves the API key from
 * {@code GEMINI_API_KEY} in the process environment, otherwise from the repo-root
 * {@code .env} file (same source Spring Boot's {@code dev} profile imports).
 */
class GeminiClientLiveTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_LIVE_TEST", matches = "true")
    void generate_returnsCompletedResponse_fromLiveApi() {
        String apiKey = resolveApiKey();
        assumeThat(apiKey)
                .as("Set GEMINI_API_KEY in the environment or in the repo-root .env")
                .isNotBlank();

        GeminiClient client = new GeminiClient(RestClient.builder(), apiKey);

        GeminiResponse response = client.generate(new GeminiRequest(
                "gemini-3.5-flash",
                "What is the OEM engine air filter part number for a 2011 Subaru Impreza 2.5i? Reply briefly.",
                null
        ));

        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.steps()).isNotEmpty();
        assertThat(response.steps().stream()
                .filter(step -> "model_output".equals(step.type()))
                .flatMap(step -> step.content() == null ? Stream.empty() : step.content().stream())
                .map(content -> content.text())
                .filter(text -> text != null && !text.isBlank())
                .findFirst())
                .isPresent();
    }

    private static String resolveApiKey() {
        String fromEnv = System.getenv("GEMINI_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return readApiKeyFromDotEnv();
    }

    /**
     * Mirrors application-dev.yml: {@code optional:file:${user.dir}/../.env[.properties]}
     * when Maven runs with {@code user.dir=backend}, plus a same-directory fallback if
     * the IDE uses the repo root as the working directory.
     */
    private static String readApiKeyFromDotEnv() {
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (Path candidate : new Path[]{userDir.resolve(".env"), userDir.resolve("..").resolve(".env").normalize()}) {
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try (Stream<String> lines = Files.lines(candidate)) {
                return lines
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .filter(line -> line.startsWith("GEMINI_API_KEY="))
                        .map(line -> line.substring("GEMINI_API_KEY=".length()).trim())
                        .map(value -> stripOptionalQuotes(value))
                        .filter(value -> !value.isBlank())
                        .findFirst()
                        .orElse(null);
            } catch (IOException ignored) {
                // try next candidate
            }
        }
        return null;
    }

    private static String stripOptionalQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
