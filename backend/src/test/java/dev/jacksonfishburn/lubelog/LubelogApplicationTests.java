package dev.jacksonfishburn.lubelog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {
		"KEYCLOAK_JWK_SET_URI=http://localhost:8080/realms/lubelog/protocol/openid-connect/certs",
		"KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/lubelog"
})
class LubelogApplicationTests {

	@Test
	void contextLoads() {
	}

}
