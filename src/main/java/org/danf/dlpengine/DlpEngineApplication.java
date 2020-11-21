package org.danf.dlpengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GENERAL NOTES:
 *
 * About logging: I would perhaps log a bit more in the Scanners themselves were it not for the inherent security risk of leaking IBAN and Social Security
 * Numbers to production logs.
 * Were this a real service intended for production use, I would write logic to redact the sensitive information (using the same matchers) and scrub the data
 * of any other identifying information before logging.
 *
 *
 */
@SpringBootApplication
public class DlpEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(DlpEngineApplication.class, args);
	}
}
