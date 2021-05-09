package ece448.iot_hub;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupsModelTests {
        
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
    private final GroupsModel groups;
    private final DatabaseController databaseController;

    public GroupsModelTests() throws Exception{
        this.mqtt = new MqttController(broker, "unit_tester/PlugsModel", topicPrefix);
        this.plugs = new PlugsModel(this.mqtt.client, topicPrefix, null);
        File dataDir = new File("./data");
		if (!dataDir.exists()){
			dataDir.mkdirs();
		}
        this.databaseController = new DatabaseController("./data/GroupsModelTests.db");
        this.groups = new GroupsModel(plugs, this.databaseController);
    }

    @Test
	public void testCreateGroup() {
        databaseController.clear();
        String groupName = new String("New group");
        List<String> groupNames_before = groups.getGroupsNames();
        assertFalse(groupNames_before.contains(groupName));
        groups.createGroup(groupName);
        List<String> groupNames_after = groups.getGroupsNames();
        assertTrue(groupNames_after.contains(groupName));
        assertEquals(groupNames_after.size(), groupNames_before.size() + 1);

    }

    @Test
	public void testCreatePopulatedGroup() {
        databaseController.clear();
        String groupName = new String("myGroup");
        ArrayList<String> group = new ArrayList<String>();
        group.add("x");
        group.add("y");
        group.add("z");
        groups.createGroup(groupName, group);
        HashMap<String, Object> groupData = groups.getGroup(groupName);
        assertEquals(groupData.get("name"), groupName);
        ArrayList<HashMap<String, Object>> members = mapper.convertValue(groupData.get("members"), new TypeReference<ArrayList<HashMap<String, Object>>>() {});
        assertEquals(members.size(), 3);
    }

    @Test
    public void testGetUnknownGroup() {
        databaseController.clear();
        String groupName = new String("unknownGroup");
        ArrayList<HashMap<String, Object>> members = mapper.convertValue(groups.getGroup(groupName).get("members"), new TypeReference<ArrayList<HashMap<String, Object>>>() {});
        assertEquals(members.size(), 0);
    }

    @Test
    public void testRemoveGroup() {
        databaseController.clear();
        String groupName = new String("group to delete");
        groups.createGroup("Another group");
        groups.createGroup(groupName);
        List<String> groupNames = groups.getGroupsNames();
        assertTrue(groupNames.contains(groupName));
        groups.removeGroup(groupName);
        assertFalse(groups.getGroupsNames().contains(groupName));
        assertEquals(groups.getGroupsNames().size(), groupNames.size() - 1);
    }

    @Test
	public void testPublish() {
        databaseController.clear();
        String groupName = new String("myGroupPublish");
        String action = new String("action");
        ArrayList<String> group = new ArrayList<String>();
        group.add("x");
        group.add("y");
        group.add("z");
        groups.createGroup(groupName, group);
        try {Thread.sleep(200);}
        catch (Exception e) {logger.error("Sleep fail");}
        groups.publishAction(groupName, action);
        try {Thread.sleep(200);}
        catch (Exception e) {logger.error("Sleep fail");}
        assertEquals(mqtt.getLastTopic(), topicPrefix+"/action/"+group.get(group.size()-1)+"/"+action);
    }

    private static final Logger logger = LoggerFactory.getLogger(GroupsModelTests.class);
    private static final ObjectMapper mapper = new ObjectMapper();
}
