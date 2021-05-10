package iot_hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PlugsModel {
    private final MqttClient client;
    private final String topicPrefix;

    private final HashMap<String, String> states = new HashMap<>();
    private final HashMap<String, String> powers = new HashMap<>();
    private final DatabaseController databaseController;

    public PlugsModel(MqttClient mqttClient, @Value("${mqtt.topicPrefix}") String topicPrefix, DatabaseController databaseController) {
        this.client = mqttClient;
        this.topicPrefix = topicPrefix;
        this.databaseController = databaseController;
        try
        {
            this.client.subscribe(this.topicPrefix+"/update/#", this::handleUpdate);
        }
        catch (Exception e)
        {
            logger.error("Mqtt Plugs: fail to subscribe");
        }
    }

    synchronized protected void handleUpdate(String topic, MqttMessage msg) {
        String[] nameUpdate = topic.substring(topicPrefix.length()+1).split("/");
        if ((nameUpdate.length != 3) || !nameUpdate[0].equals("update"))
            return; // ignore unknown format

        switch (nameUpdate[2])
        {
        case "state":
            states.put(nameUpdate[1], msg.toString());
            break;
        case "power":
            powers.put(nameUpdate[1], msg.toString());
            if (databaseController != null) {
                databaseController.insertPower(nameUpdate[1], Float.parseFloat(msg.toString()));
            }
            break;
        default:
            return;
        }
    }

    synchronized public void publishAction(String plugName, String action) {
        String topic = topicPrefix+"/action/"+plugName+"/"+action;
        try
        {
            client.publish(topic, new MqttMessage());
        }
        catch (Exception e)
        {
            logger.error("MqttCtl: {} fail to publish", topic);
        }
    }

    synchronized public String getState(String plugName) {
        return states.get(plugName);
    }

    synchronized public String getPower(String plugName) {
        return powers.get(plugName);
    }

    synchronized public Map<String, String> getStates() {
        return new TreeMap<>(states);
    }

    synchronized public List<String> getNames() {
        return new ArrayList<String>(states.keySet());
    }

    synchronized public Map<String, String> getPowers() {
        return new TreeMap<>(powers);
    }

    synchronized public HashMap<String, Object> getPlug(String plugName) {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("name", plugName);
        ret.put("state", states.get(plugName));
        ret.put("power", powers.get(plugName));
        return ret;
    }

    private static final Logger logger = LoggerFactory.getLogger(PlugsModel.class);
}
