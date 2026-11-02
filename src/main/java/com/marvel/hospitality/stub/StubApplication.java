package com.marvel.hospitality.stub;

import com.marvel.hospitality.stub.config.WireMockProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesSource
@EnableConfigurationProperties(WireMockProperties.class)
public class StubApplication {

	public static void main(String[] args) {
		SpringApplication.run(StubApplication.class, args);
	}

}
