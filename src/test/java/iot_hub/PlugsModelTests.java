package iot_hub;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlugsModelTests {
        
    public static class MqttController {
        private String lastTopic = new String();
        private String lastMessage = new String();

        private final MqttClient client;
    
        public MqttController(String broker, String clientId,
            String topicPrefix) throws Exception {
            this.client = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions opt = new MqttConnectOptions();
            opt.setCleanSession(true);
            client.connect(opt);
            
            client.subscribe(topicPrefix+"/#", this::handleUpdate);
        }

        synchronized protected void handleUpdate(String topic, MqttMessage msg) {
            lastTopic = topic;
            lastMessage = msg.toString();
        }

        synchronized public String getLastTopic() {
			return lastTopic;
        }
        
        synchronized public String getLastMessage() {
			return lastMessage;
		}
    }

    private final MqttController mqtt;
    private final String topicPrefix = new String("unit_tests/PlugsModel");
    private final String broker = new String("tcp://127.0.0.1");
    private final PlugsModel plugs;

    public PlugsModelTests() throws Exception{
        this.mqtt = new MqttController(broker, "unit_tester/PlugsModel", topicPrefix);
        this.plugs = new PlugsModel(this.mqtt.client, topicPrefix, null);
    }

    @Test
	public void testStoppedClient() throws Exception{
        String plugName = new String("name");
        String action = new String("action");
        String topic = mqtt.getLastTopic();
        PlugsModel plugsError = new PlugsModel(new MqttClient(broker, "non_working" , new MemoryPersistence()), topicPrefix, null);
        plugsError.publishAction(plugName, action);
        assertTrue(topic.equals(mqtt.getLastTopic()));
    }

    @Test
	public void testInit() {
        String plugName = new String("name");
        String action = new String("action");
        plugs.publishAction(plugName, action);
        try {Thread.sleep(1000);}
        catch (Exception e) {logger.error("Sleep fail");}
        assertTrue(mqtt.getLastTopic().equals(topicPrefix+"/action/"+plugName+"/"+action));
    }

    @Test
	public void testStateUpdate() {
        String plugName = new String("plug");
        String[] states = {"on", "off", "off", "on"};
        for (String state : states) {
            plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/state", new MqttMessage(state.getBytes()));
            assertTrue(plugs.getState(plugName).equals(state));   
        }
    }

    @Test
	public void testPowerUpdate() {
        String plugName = new String("plug");
        String[] powers = {"42", "160.500", "0.0", "2435.1243890"};
        for (String power : powers) {
            plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/power", new MqttMessage(power.getBytes()));
            assertTrue(plugs.getPower(plugName).equals(power));   
        }
    }
    
    @Test
	public void testErrorUpdate() {
        String plugName = new String("plug");
        String state = "on";
        plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/state", new MqttMessage(state.getBytes()));

        plugs.handleUpdate(topicPrefix+"/not_update/"+plugName+"/power", new MqttMessage("off".getBytes()));
        plugs.handleUpdate(topicPrefix+"/update/extra/"+plugName+"/power", new MqttMessage("off".getBytes()));
        plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/nonexistent", new MqttMessage("off".getBytes()));
        assertTrue(plugs.getState(plugName).equals(state));   
    }

    @Test
	public void testGetAll() {
        PlugsModel plugsFresh = new PlugsModel(mqtt.client, topicPrefix, null);
        HashMap<String, String> states = new HashMap<>();
        HashMap<String, String> powers = new HashMap<>();
        List<String> names = new ArrayList<String>();
        String[] plugNames = {"Alex", "Charles", "Bea"};
        String[] plugStates = {"on", "on", "off"};
        String[] plugPowers = {"142.00", "96", "0.0"};
        for (int i = 0; i < plugNames.length; i++) {
            names.add(plugNames[i]);
            states.put(plugNames[i], plugStates[i]);
            powers.put(plugNames[i], plugPowers[i]);
            plugsFresh.handleUpdate(topicPrefix+"/update/"+plugNames[i]+"/state", new MqttMessage(plugStates[i].getBytes()));
            plugsFresh.handleUpdate(topicPrefix+"/update/"+plugNames[i]+"/power", new MqttMessage(plugPowers[i].getBytes()));
        }
        try {Thread.sleep(1000);}
        catch (Exception e) {logger.error("Sleep fail");}

        assertTrue(plugsFresh.getNames().equals(names));
        assertTrue(plugsFresh.getStates().equals(states));
        assertTrue(plugsFresh.getPowers().equals(powers));
    }

    private static final Logger logger = LoggerFactory.getLogger(PlugsModelTests.class);

}
