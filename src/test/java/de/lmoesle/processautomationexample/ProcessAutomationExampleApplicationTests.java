package de.lmoesle.processautomationexample;

import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
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
	private UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;

	@MockitoBean
	private UrlaubsantragGenehmigungsprozessStartenOutPort genehmigungsprozessStartenOutPort;

	@MockitoBean
	private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;

	@MockitoBean
	private BenutzerRepositoryOutPort benutzerRepositoryOutPort;

	@Test
	void contextLoads() {
	}

}
