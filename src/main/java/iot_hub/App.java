package iot_hub;

import java.io.File;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class App {
	@Autowired
	public App(Environment env) throws Exception {
	}

	@Bean(destroyMethod = "disconnect")
	public MqttClient mqttClient(Environment env) throws Exception {
		String broker = env.getProperty("mqtt.broker");
		String clientId = env.getProperty("mqtt.clientId");
		MqttClient mqtt = new MqttClient(broker, clientId, new MemoryPersistence());
		mqtt.connect();
		logger.info("MqttClient {} connected: {}", clientId, broker);
		return mqtt;
	}

	@Bean
	public DatabaseController databaseController(@Value("${database.fileName}") String databaseFileName) {
		File dataDir = new File("./data");
		if (!dataDir.exists()){
			dataDir.mkdirs();
		}
		DatabaseController dbc = new DatabaseController("./data/"+databaseFileName);
		return dbc;
	}

	private static final Logger logger = LoggerFactory.getLogger(App.class);
}
