package com.piotrgrochowiecki.manager;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@Log4j2
public class FinancialInstrumentsSubscriptionsManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialInstrumentsSubscriptionsManagerApplication.class, args);
	}

}
