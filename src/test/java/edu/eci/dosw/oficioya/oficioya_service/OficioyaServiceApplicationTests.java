package edu.eci.dosw.oficioya.oficioya_service;

import edu.eci.dosw.oficioya.oficioya_service.repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class OficioyaServiceApplicationTests {

	@MockitoBean
	ImageRepository imageRepository;

	@Test
	void contextLoads() {
	}

}