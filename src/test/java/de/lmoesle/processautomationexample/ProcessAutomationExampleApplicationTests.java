package de.lmoesle.processautomationexample;

import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ProcessAutomationExampleApplicationTests {

	@MockitoBean
	private SaveVacationRequestOutPort saveVacationRequestOutPort;

	@MockitoBean
	private StartVacationApprovalProcessOutPort startVacationApprovalProcessOutPort;

	@Test
	void contextLoads() {
	}

}
