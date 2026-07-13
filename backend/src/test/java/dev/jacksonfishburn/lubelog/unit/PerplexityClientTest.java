package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import dev.jacksonfishburn.lubelog.client.PerplexityClient;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.exception.AiApiAccessException;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PerplexityClientTest {

    private static final String API_KEY = "test-perplexity-key";
    private static final String RESPONSES_URL = "https://api.perplexity.ai/v1/responses";

    private MockRestServiceServer mockServer;
    private PerplexityClient perplexityClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        perplexityClient = new PerplexityClient(builder, API_KEY);
    }

    @Test
    void search_returnsParsedResponse_whenPerplexityCompletes() {
        mockServer.expect(requestTo(RESPONSES_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andExpect(content().json("""
                        {
                          "input": "Find air filter purchase links for a 2011 Subaru Impreza 2.5i",
                          "stream": false,
                          "preset": "pro-search",
                          "instructions": "Return OEM, performance, and aftermarket options."
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "id": "resp_123",
                          "status": "completed",
                          "output": [
                            {
                              "type": "search_results",
                              "queries": [
                                "2011 Subaru Impreza 2.5i engine air filter buy links"
                              ],
                              "results": [
                                {
                                  "id": 1,
                                  "title": "OEM Air Filter",
                                  "url": "https://www.subarupartsdeal.com/oem-subaru-impreza-air_filter.html",
                                  "snippet": "Genuine Subaru air filter",
                                  "source": "web",
                                  "date": "2026-01-01",
                                  "last_updated": "2026-02-16"
                                },
                                {
                                  "id": 2,
                                  "title": "K&N Air Filter",
                                  "url": "https://www.knfilters.com/33-2304-replacement-air-filter",
                                  "snippet": "Performance washable filter",
                                  "source": "web",
                                  "date": "2026-01-01",
                                  "last_updated": "2026-02-16"
                                }
                              ]
                            },
                            {
                              "type": "message",
                              "role": "assistant",
                              "status": "completed",
                              "content": [
                                {
                                  "type": "output_text",
                                  "text": "Here are three purchase links for air filters."
                                }
                              ]
                            }
                          ],
                          "usage": {
                            "input_tokens": 6061,
                            "output_tokens": 1386,
                            "total_tokens": 7447,
                            "cost": {
                              "currency": "USD",
                              "total_cost": 0.01219
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PerplexitySearchResponse response = perplexityClient.search(new PerplexitySearchRequest(
                "Find air filter purchase links for a 2011 Subaru Impreza 2.5i",
                false,
                "pro-search",
                "Return OEM, performance, and aftermarket options."
        ));

        assertThat(response.id()).isEqualTo("resp_123");
        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.links()).containsExactly(
                "https://www.subarupartsdeal.com/oem-subaru-impreza-air_filter.html",
                "https://www.knfilters.com/33-2304-replacement-air-filter"
        );
        assertThat(response.answerText()).isEqualTo("Here are three purchase links for air filters.");
        assertThat(response.usage().totalTokens()).isEqualTo(7447);
        assertThat(response.usage().cost().totalCost()).isEqualTo(0.01219);
        mockServer.verify();
    }

    @Test
    void search_throwsAiFailureException_whenStatusIsNotCompleted() {
        mockServer.expect(requestTo(RESPONSES_URL))
                .andRespond(withSuccess("""
                        {
                          "id": "resp_failed",
                          "status": "failed",
                          "output": [],
                          "usage": null
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> perplexityClient.search(new PerplexitySearchRequest(
                "find parts",
                false,
                "pro-search",
                null
        )))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("failed");
        mockServer.verify();
    }

    @Test
    void search_throwsAiApiAccessException_whenPerplexityReturnsErrorStatus() {
        mockServer.expect(requestTo(RESPONSES_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> perplexityClient.search(new PerplexitySearchRequest(
                "find parts",
                false,
                "pro-search",
                null
        )))
                .isInstanceOf(AiApiAccessException.class)
                .hasMessageContaining("find parts");
        mockServer.verify();
    }
}
