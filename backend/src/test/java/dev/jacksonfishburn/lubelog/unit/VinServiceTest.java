package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import dev.jacksonfishburn.lubelog.service.VinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import dev.jacksonfishburn.lubelog.client.VinClient;
import dev.jacksonfishburn.lubelog.dto.VinDecodeResponse;
import dev.jacksonfishburn.lubelog.exception.VinLookupException;

class VinServiceTest {

    private static final String VIN = "1HGCM82633A004352";
    private static final String DECODE_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + VIN + "?format=json";

    private MockRestServiceServer mockServer;
    private VinService vinService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        vinService = new VinService(new VinClient(builder));
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

        VinDecodeResponse response = vinService.decodeVin(VIN);

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

        assertThatThrownBy(() -> vinService.decodeVin(VIN))
                .isInstanceOf(VinLookupException.class)
                .hasMessageContaining(VIN);
        mockServer.verify();
    }

    @Test
    void decodeVin_throwsVinLookupException_whenNhtsaReturnsNonSuccessStatus() {
        mockServer.expect(requestTo(DECODE_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> vinService.decodeVin(VIN))
                .isInstanceOf(VinLookupException.class)
                .hasMessageContaining(VIN);
        mockServer.verify();
    }
}
