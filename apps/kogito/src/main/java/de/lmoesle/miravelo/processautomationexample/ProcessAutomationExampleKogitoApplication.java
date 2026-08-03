package de.lmoesle.miravelo.processautomationexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"de.lmoesle.miravelo.processautomationexample",
	"org.kie.kogito"
})
public class ProcessAutomationExampleKogitoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessAutomationExampleKogitoApplication.class, args);
	}

}
