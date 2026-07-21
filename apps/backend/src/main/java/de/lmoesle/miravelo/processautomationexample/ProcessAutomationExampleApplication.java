package de.lmoesle.miravelo.processautomationexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProcessAutomationExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessAutomationExampleApplication.class, args);
	}

}
