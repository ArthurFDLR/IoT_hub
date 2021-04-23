package ece448.iot_hub;

import java.io.File;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class Main implements AutoCloseable {

	public static void main(String[] args) throws Exception {
		// load configuration file
		String configFile = args.length > 0 ? args[0] : "hubConfig.json";
		HubConfig config = mapper.readValue(new File(configFile), HubConfig.class);
		logger.info("{}: {}", configFile, mapper.writeValueAsString(config));

		try (Main m = new Main(config, args))
		{
			// loop forever
			for (;;)
			{
				Thread.sleep(60000);
			}
		}
	}

	public Main(HubConfig config, String[] args) throws Exception {
		// Spring app
		HashMap<String, Object> props = new HashMap<>();
		props.put("server.port", config.getHttpPort());
		props.put("mqtt.broker", config.getMqttBroker());
		props.put("mqtt.clientId", config.getMqttClientId());
		props.put("mqtt.topicPrefix", config.getMqttTopicPrefix());
		SpringApplication app = new SpringApplication(App.class);
		app.setDefaultProperties(props);
		this.appCtx = app.run(args);
	}

	@Override
	public void close() throws Exception {
		appCtx.close();
	}

	private final ConfigurableApplicationContext appCtx;

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(Main.class);	
}
