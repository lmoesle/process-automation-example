package de.lmoesle.processautomationexample;

import de.lmoesle.processautomationexample.application.ports.out.LoadVacationRequestsOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UserRepositoryOutPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
	"dev.bpm-crafters.process-api.worker.enabled=false",
	"dev.bpm-crafters.process-api.worker.register-process-workers=false",
	"dev.bpm-crafters.process-api.adapter.c7embedded.enabled=false"
})
class ProcessAutomationExampleApplicationTests {

	@MockitoBean
	private SaveVacationRequestOutPort saveVacationRequestOutPort;

	@MockitoBean
	private StartVacationApprovalProcessOutPort startVacationApprovalProcessOutPort;

	@MockitoBean
	private LoadVacationRequestsOutPort loadVacationRequestsOutPort;

	@MockitoBean
	private UserRepositoryOutPort userRepositoryOutPort;

	@Test
	void contextLoads() {
	}

}
