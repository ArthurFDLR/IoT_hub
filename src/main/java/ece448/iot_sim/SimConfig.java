package ece448.iot_sim;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimConfig {

	private final int httpPort;
	private final List<String> plugNames;
	private final String mqttBroker;
	private final String mqttClientId;
	private final String mqttTopicPrefix;

	@JsonCreator
	public SimConfig(
		@JsonProperty(value = "httpPort", required = true) int httpPort,
		@JsonProperty(value = "plugNames", required = true) List<String> plugNames,
		@JsonProperty(value = "mqttBroker", required = false) String mqttBroker,
		@JsonProperty(value = "mqttClientId", required = false) String mqttClientId,
		@JsonProperty(value = "mqttTopicPrefix", required = false) String mqttTopicPrefix) {
		this.httpPort = httpPort;
		this.plugNames = plugNames;
		this.mqttBroker = mqttBroker;
		this.mqttClientId = mqttClientId;
		this.mqttTopicPrefix = mqttTopicPrefix;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public List<String> getPlugNames() {
		return plugNames;
	}

	public String getMqttBroker() {
		return mqttBroker;
	}

	public String getMqttClientId() {
		return mqttClientId;
	}

	public String getMqttTopicPrefix() {
		return mqttTopicPrefix;
	}
}
