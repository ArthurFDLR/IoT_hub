package iot_hub;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlugsResourceTests {
    
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

    private static final ObjectMapper mapper = new ObjectMapper();

    private final MqttController mqtt;
    private final String topicPrefix = new String("unit_tests/PlugsModel");
    private final String broker = new String("tcp://127.0.0.1");
    private final PlugsModel plugs;
    private final PlugsResource plugsRes;

    public PlugsResourceTests() throws Exception{
        this.mqtt = new MqttController(broker, "unit_tester/PlugsModel", topicPrefix);
        this.plugs = new PlugsModel(this.mqtt.client, topicPrefix, null);
        this.plugsRes = new PlugsResource(this.plugs);
    }

    @Test
    public void testGetPlug() throws Exception{
        String plugName = new String("plug_name");
        String state = new String("on");
        String power = new String("42.960");

        Map<String, String> plugJSON = mapper.convertValue(plugsRes.getPlug(plugName, null), new TypeReference<Map<String, String>>(){});
        assertTrue(plugJSON.get("power") == null);
        assertTrue(plugJSON.get("state") == null);

        plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/state", new MqttMessage(state.getBytes()));
        plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/power", new MqttMessage(power.getBytes()));
        plugJSON = mapper.convertValue(plugsRes.getPlug(plugName, null), new TypeReference<Map<String, String>>(){});
        assertTrue(plugJSON.get("power").equals(power));
        assertTrue(plugJSON.get("state").equals(state));
    }

    @Test
    public void testGetPlugAction() throws Exception{
        String plugName = new String("plug_name");
        String state = new String("on");
        String power = new String("42.960");
        String action = new String("some_action");

        plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/state", new MqttMessage(state.getBytes()));
        plugs.handleUpdate(topicPrefix+"/update/"+plugName+"/power", new MqttMessage(power.getBytes()));
        Map<String, String> plugJSON = mapper.convertValue(plugsRes.getPlug(plugName, action), new TypeReference<Map<String, String>>(){});
        assertTrue(plugJSON.get("power").equals(power));
        assertTrue(plugJSON.get("state").equals(state));
        try {Thread.sleep(1000);}
        catch (Exception e) {logger.error("Sleep fail");}
        assertTrue(mqtt.getLastTopic().equals(topicPrefix+"/action/"+plugName+"/"+action));
    }

    @Test
    public void testGetPlugsEmpty() throws Exception{
        List<Map<String, String>> plugJSON = mapper.convertValue(plugsRes.getPlugs(), new TypeReference<List<Map<String, String>>>() {});
        assertTrue(plugJSON.size() == plugs.getNames().size());
    }

    @Test
    public void testGetPlugs() throws Exception{        
        String[] plugNames = {"Alex", "Charles", "Bea"};
        String[] plugStates = {"on", "on", "off"};
        String[] plugPowers = {"142.00", "96", "0.0"};
        for (int i = 0; i < plugNames.length; i++) {
            plugs.handleUpdate(topicPrefix+"/update/"+plugNames[i]+"/state", new MqttMessage(plugStates[i].getBytes()));
            plugs.handleUpdate(topicPrefix+"/update/"+plugNames[i]+"/power", new MqttMessage(plugPowers[i].getBytes()));
        }
        try {Thread.sleep(1000);}
        catch (Exception e) {logger.error("Sleep fail");}

        List<Map<String, String>> plugJSON = mapper.convertValue(plugsRes.getPlugs(), new TypeReference<List<Map<String, String>>>() {});
        assertTrue(plugJSON.size() == plugNames.length);
        for (int i = 0; i < plugJSON.size(); i++) {
            assertTrue(plugJSON.get(i).get("name").equals(plugNames[i]));
            assertTrue(plugJSON.get(i).get("power").equals(plugPowers[i]));
            assertTrue(plugJSON.get(i).get("state").equals(plugStates[i]));
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(PlugsResourceTests.class);

}
