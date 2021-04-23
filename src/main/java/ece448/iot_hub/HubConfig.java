package ece448.iot_hub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HubConfig {

	private final int httpPort;
	private final String mqttBroker;
	private final String mqttClientId;
	private final String mqttTopicPrefix;

	@JsonCreator
	public HubConfig(
		@JsonProperty(value = "httpPort", required = true) int httpPort,
		@JsonProperty(value = "mqttBroker", required = true) String mqttBroker,
		@JsonProperty(value = "mqttClientId", required = true) String mqttClientId,
		@JsonProperty(value = "mqttTopicPrefix", required = true) String mqttTopicPrefix) {
		this.httpPort = httpPort;
		this.mqttBroker = mqttBroker;
		this.mqttClientId = mqttClientId;
		this.mqttTopicPrefix = mqttTopicPrefix;
	}

	public int getHttpPort() {
		return httpPort;
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
