package bm25;

import bm25.utils.Utils;
import jdk.jshell.execution.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Objects;

@SpringBootApplication
public class BM25ServiceApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(BM25ServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BM25ServiceApplication.class, args);
	}

	@PreDestroy
	public void destroy() {
		LOGGER.info("Saving keyToIndexMapFile before shutdown");
		try {
			Utils.saveKeyToIndexMap();
		} catch (Exception e) {
			LOGGER.error("Failed to write keyToIndexMapFile", e);
		}
		LOGGER.info("Done!");
	}

	@Bean
	public CommandLineRunner runner(Environment environment) {
		return (args) -> Utils.setKeyToIndexMapFile(Objects.requireNonNull(environment.getProperty("main.keyToIndexMapFile")));
	}
}
