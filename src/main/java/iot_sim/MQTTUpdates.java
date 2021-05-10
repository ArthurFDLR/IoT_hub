package iot_sim;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTUpdates {
    private final String topicPrefix;
    public MQTTUpdates(String topicPrefix){
        this.topicPrefix = topicPrefix;
    }
    public String getTopic(String name, String key){
        return topicPrefix+"/update/"+name+"/"+key;
    }
    public MqttMessage getMessage(String value){
        MqttMessage msg = new MqttMessage(value.getBytes());
        msg.setRetained(true);
        return msg;
    }
}
