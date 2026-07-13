package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import dev.jacksonfishburn.lubelog.client.PerplexityClient;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

/**
 * Opt-in smoke test against the real Perplexity API.
 *
 * <p>Skipped unless {@code PERPLEXITY_LIVE_TEST=true}. Resolves the API key from
 * {@code PERPLEXITY_API_KEY} in the process environment, otherwise from the repo-root
 * {@code .env} file (same source Spring Boot's {@code dev} profile imports).
 */
class PerplexityClientLiveTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "PERPLEXITY_LIVE_TEST", matches = "true")
    void search_returnsCompletedResponse_fromLiveApi() {
        String apiKey = resolveApiKey();
        assumeThat(apiKey)
                .as("Set PERPLEXITY_API_KEY in the environment or in the repo-root .env")
                .isNotBlank();

        PerplexityClient client = new PerplexityClient(RestClient.builder(), apiKey);

        PerplexitySearchResponse response = client.search(new PerplexitySearchRequest(
                "What is the OEM engine air filter part number for a 2011 Subaru Impreza 2.5i? Reply briefly.",
                false,
                "pro-search",
                "Be concise. Include a couple of purchase-relevant links if available."
        ));

        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.answerText()).isNotBlank();
        assertThat(response.output()).isNotEmpty();
        assertThat(response.links()).isNotEmpty();
    }

    private static String resolveApiKey() {
        String fromEnv = System.getenv("PERPLEXITY_API_KEY");
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
                        .filter(line -> line.startsWith("PERPLEXITY_API_KEY="))
                        .map(line -> line.substring("PERPLEXITY_API_KEY=".length()).trim())
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
