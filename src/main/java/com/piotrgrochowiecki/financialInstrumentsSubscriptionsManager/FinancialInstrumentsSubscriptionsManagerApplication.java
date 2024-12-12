package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@Log4j2
public class FinancialInstrumentsSubscriptionsManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialInstrumentsSubscriptionsManagerApplication.class, args);
	}

}
