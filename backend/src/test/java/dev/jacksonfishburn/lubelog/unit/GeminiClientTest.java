package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.node.TextNode;
import dev.jacksonfishburn.lubelog.client.GeminiClient;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import dev.jacksonfishburn.lubelog.exception.AiApiAccessException;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiClientTest {

    private static final String API_KEY = "test-gemini-key";
    private static final String INTERACTIONS_URL =
            "https://generativelanguage.googleapis.com/v1beta/interactions";

    private MockRestServiceServer mockServer;
    private GeminiClient geminiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        geminiClient = new GeminiClient(builder, API_KEY);
    }

    @Test
    void generate_returnsParsedResponse_whenGeminiCompletes() {
        mockServer.expect(requestTo(INTERACTIONS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().json("""
                        {
                          "model": "gemini-3.5-flash",
                          "input": "Summarize air filter options for a 2011 Subaru Impreza 2.5i",
                          "response_format": "text"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "id": "int_123",
                          "status": "completed",
                          "steps": [
                            {
                              "type": "model_output",
                              "content": [
                                {
                                  "type": "text",
                                  "text": "OEM and aftermarket air filters are both available for this vehicle."
                                }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GeminiResponse response = geminiClient.generate(new GeminiRequest(
                "gemini-3.5-flash",
                "Summarize air filter options for a 2011 Subaru Impreza 2.5i",
                TextNode.valueOf("text")
        ));

        assertThat(response.id()).isEqualTo("int_123");
        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.steps()).hasSize(1);
        assertThat(response.outputText())
                .isEqualTo("OEM and aftermarket air filters are both available for this vehicle.");
        mockServer.verify();
    }

    @Test
    void generate_throwsAiFailureException_whenStatusIsNotCompleted() {
        mockServer.expect(requestTo(INTERACTIONS_URL))
                .andRespond(withSuccess("""
                        {
                          "id": "int_failed",
                          "status": "failed",
                          "steps": []
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> geminiClient.generate(new GeminiRequest(
                "gemini-3.5-flash",
                "summarize parts",
                null
        )))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("failed");
        mockServer.verify();
    }

    @Test
    void generate_throwsAiApiAccessException_whenGeminiReturnsErrorStatus() {
        mockServer.expect(requestTo(INTERACTIONS_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> geminiClient.generate(new GeminiRequest(
                "gemini-3.5-flash",
                "summarize parts",
                null
        )))
                .isInstanceOf(AiApiAccessException.class)
                .hasMessageContaining("summarize parts");
        mockServer.verify();
    }
}
