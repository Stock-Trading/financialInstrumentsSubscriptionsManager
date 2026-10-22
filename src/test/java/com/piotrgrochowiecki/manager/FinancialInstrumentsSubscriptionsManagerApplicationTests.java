package com.piotrgrochowiecki.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FinancialInstrumentsSubscriptionsManagerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("""
			Given Java 25 entry point declaration,
			when checking the main method visibility,
			then should not be public because Java does not require it anymore
			""")
	void should_mainMethodBePackagePrivate() throws NoSuchMethodException {
		Method mainMethod = FinancialInstrumentsSubscriptionsManagerApplication.class.getDeclaredMethod("main", String[].class);

		assertThat(Modifier.isPublic(mainMethod.getModifiers())).isFalse();
	}

}
