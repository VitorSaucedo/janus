package com.vitorsaucedo.janus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JanusApplicationTests {

	@Test
	void contextLoads() {
	}

}
