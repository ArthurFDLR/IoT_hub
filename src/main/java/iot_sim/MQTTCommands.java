package iot_sim;

import java.util.List;
import java.util.TreeMap;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTCommands {
    private final TreeMap<String, PlugSim> plugs = new TreeMap<>();
    private final String topicPrefix;

    public MQTTCommands(List<PlugSim> plugs, String topicPrefix) {
        for (PlugSim plug: plugs)
            this.plugs.put(plug.getName(), plug);
        this.topicPrefix = topicPrefix;
    }

    public String getTopic(){
        return topicPrefix+"/action/#";
    }

    public void handleMessage(String topic, MqttMessage msg) {
        logger.info("MqttCmd {}", topic);

        String[] topicSplit = topic.split("/");
        PlugSim plug = plugs.get(topicSplit[topicSplit.length - 2]);

		if (plug != null){
            if ("action".equals(topicSplit[topicSplit.length - 3])) {
                switch (topicSplit[topicSplit.length - 1]) {
                    case "toggle":
                        plug.toggle();
                        break;
                    case "on":
                        plug.switchOn();
                        break;
                    case "off":
                        plug.switchOff();
                        break;
                    default:
                        break;
                }
            }
        }
    }

	private static final Logger logger = LoggerFactory.getLogger(HTTPCommands.class);
}
