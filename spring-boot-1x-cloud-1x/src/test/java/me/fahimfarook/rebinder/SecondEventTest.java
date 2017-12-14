package me.fahimfarook.rebinder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationSecondEvent.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecondEventTest {

	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	private ConfigurableEnvironment environment;
	
	@Before
	public void fireFisrtEvent() {
		// lets fire the first EnvironmentChangeEvent because it used to fail from the second event only.
		EnvironmentTestUtils.addEnvironment(this.environment, "second.setup=setup-val");
		this.context.publishEvent(new EnvironmentChangeEvent(Collections.singleton("second.setup")));
	}
	
	@Test
	public void updateStrWithSecondEvent() {		
		EnvironmentTestUtils.addEnvironment(this.environment, "second.str=str-val");
		assertEquals(this.environment.getProperty("second.str"), "str-val");	
		this.context.publishEvent(new EnvironmentChangeEvent(Collections.singleton("second.str")));
		assertEquals("Config bean isn't updated for prop = str, event# = 2", "str-val", this.context.getBean(PropsSecond.class).getStr());
	}
	
	@Test
	public void updateListWithSecondEvent() {
		EnvironmentTestUtils.addEnvironment(this.environment, "second.list=item1,ites2");
		assertEquals(this.environment.getProperty("second.list"), "item1,ites2");	
		this.context.publishEvent(new EnvironmentChangeEvent(Collections.singleton("second.list")));
		assertEquals("Config bean isn't updated for prop = list, event# = 2", "item1", this.context.getBean(PropsSecond.class).getList().get(0));		
	}
	
	@Test
	public void updateMapWithSecondEvent() {
		EnvironmentTestUtils.addEnvironment(this.environment, "second.map.key1=map-val1");
		assertEquals(this.environment.getProperty("second.map.key1"), "map-val1");	
		this.context.publishEvent(new EnvironmentChangeEvent(Collections.singleton("second.map.key1")));
		assertEquals("Config bean isn't updated for prop = map, event# = 2", "map-val1", this.context.getBean(PropsSecond.class).getMap().get("key1"));		
	}
	
	@Test
	public void updateMapWithSecondEventAfterWainting() {		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {}
		
		EnvironmentTestUtils.addEnvironment(this.environment, "second.map.key2=map-val2");
		assertEquals(this.environment.getProperty("second.map.key2"), "map-val2");	
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {}
		
		this.context.publishEvent(new EnvironmentChangeEvent(Collections.singleton("second.map.key2")));
		assertEquals("Config bean isn't updated for prop = map, event# = 2", "map-val2", this.context.getBean(PropsSecond.class).getMap().get("key2"));		
	}
}

@Configuration
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
class ApplicationSecondEvent {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ApplicationSecondEvent.class).run(args);
	}
	
	@Bean
	public PropsSecond propsSecond() {
		return new PropsSecond();
	}
}

@ConfigurationProperties(prefix = "second")
class PropsSecond {
	private String setup;
	
	private String str;
		
	private Map<String, String> map = new HashMap<>();

	private List<String> list = new ArrayList<>();

	public String getSetup() {
		return setup;
	}

	public void setSetup(String setup) {
		this.setup = setup;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}
}
