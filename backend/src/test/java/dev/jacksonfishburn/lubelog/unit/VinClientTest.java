package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import dev.jacksonfishburn.lubelog.client.VinClient;
import dev.jacksonfishburn.lubelog.dto.VinDecodeResponse;
import dev.jacksonfishburn.lubelog.exception.VinLookupException;

/**
 * VinClient deserializes NHTSA's response into a private nested record, so Mockito cannot stub
 * RestClient's fluent {@code .body(NhtsaDecodeResponse.class)} call with a populated instance
 * from outside the class (the type isn't accessible here, and a mocked return value of the wrong
 * type would fail VinClient's own checked cast). MockRestServiceServer sidesteps this by faking
 * the HTTP layer instead: VinClient's real RestClient and real Jackson deserialization run
 * unmodified against a canned response body, so no Spring context is started.
 */
class VinClientTest {

    private static final String VIN = "1HGCM82633A004352";
    private static final String DECODE_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + VIN + "?format=json";

    private MockRestServiceServer mockServer;
    private VinClient vinClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        vinClient = new VinClient(builder);
    }

    @Test
    void decodeVin_returnsPopulatedResponse_whenNhtsaReturnsAResult() {
        mockServer.expect(requestTo(DECODE_URL))
                .andRespond(withSuccess("""
                        {
                          "Results": [
                            {
                              "Make": "HONDA",
                              "Model": "Accord",
                              "ModelYear": "2003",
                              "Trim": "EX"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        VinDecodeResponse response = vinClient.decodeVin(VIN);

        assertThat(response.year()).isEqualTo((short) 2003);
        assertThat(response.make()).isEqualTo("HONDA");
        assertThat(response.model()).isEqualTo("Accord");
        assertThat(response.trim()).isEqualTo("EX");
        mockServer.verify();
    }

    @Test
    void decodeVin_throwsVinLookupException_whenNhtsaReturnsEmptyResults() {
        mockServer.expect(requestTo(DECODE_URL))
                .andRespond(withSuccess("""
                        {
                          "Results": []
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> vinClient.decodeVin(VIN))
                .isInstanceOf(VinLookupException.class)
                .hasMessageContaining(VIN);
        mockServer.verify();
    }

    @Test
    void decodeVin_throwsVinLookupException_whenNhtsaReturnsNonSuccessStatus() {
        mockServer.expect(requestTo(DECODE_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> vinClient.decodeVin(VIN))
                .isInstanceOf(VinLookupException.class)
                .hasMessageContaining(VIN);
        mockServer.verify();
    }
}
