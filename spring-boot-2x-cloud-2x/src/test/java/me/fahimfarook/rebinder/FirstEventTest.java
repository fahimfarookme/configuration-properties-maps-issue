package me.fahimfarook.rebinder;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationFirstEvent.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FirstEventTest {

	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	private ConfigurableEnvironment environment;
	
	@Test
	public void updateMapWithFirstEvent() {		
		TestPropertyValues.of("first.map.key=map-val").applyTo(this.environment);
		assertEquals(this.environment.getProperty("first.map.key"), "map-val");	
		this.context.publishEvent(new EnvironmentChangeEvent(Collections.singleton("first.map.key")));
		assertEquals("Config bean isn't updated for prop = map, event# = 1", "map-val", this.context.getBean(PropsFirst.class).getMap().get("key"));		
	}
}

@Configuration
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
class ApplicationFirstEvent {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ApplicationFirstEvent.class).run(args);
	}
	
	@Bean
	public PropsFirst propsFirst() {
		return new PropsFirst();
	}
}

@ConfigurationProperties(prefix = "first")
class PropsFirst {	
	private Map<String, String> map = new HashMap<>();

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}