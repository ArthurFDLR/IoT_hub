package iot_hub;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

public class GroupsResourceTests {

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
    private final GroupsModel groups;
    private final GroupsResource groupsRes;
    private final DatabaseController databaseController;

    public GroupsResourceTests() throws Exception{
        this.mqtt = new MqttController(broker, "unit_tester/PlugsModel", topicPrefix);
        this.plugs = new PlugsModel(this.mqtt.client, topicPrefix, null);
        File dataDir = new File("./data");
		if (!dataDir.exists()){
			dataDir.mkdirs();
		}
        this.databaseController = new DatabaseController("./data/GroupsResourceTests.db");
        this.groups = new GroupsModel(plugs, this.databaseController);
        this.groupsRes = new GroupsResource(groups);
    }

    @Test
    public void testGetGroup() throws Exception{
        databaseController.clear();
        String groupName = new String("myGroup");
        String action = new String("action");
        ArrayList<String> group = new ArrayList<String>();
        group.add("x");
        group.add("y");
        group.add("z");
        groups.createGroup(groupName, group);
        try {Thread.sleep(200);}
        catch (Exception e) {}
        
        Map<String, Object> groupJSON = mapper.convertValue(groupsRes.getGroup(groupName, null), new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> members = mapper.convertValue(groupJSON.get("members"), new TypeReference<List<Map<String, Object>>>() {});
        try {Thread.sleep(200);}
        catch (Exception e) {}
        assertEquals(members.size(), 3);
        
        groupJSON = mapper.convertValue(groupsRes.getGroup(groupName, action), new TypeReference<Map<String, Object>>() {});
        members = mapper.convertValue(groupJSON.get("members"), new TypeReference<List<Map<String, Object>>>() {});
        try {Thread.sleep(200);}
        catch (Exception e) {}
        assertEquals(members.size(), 3);
        assertEquals(mqtt.getLastTopic(), topicPrefix+"/action/"+group.get(group.size()-1)+"/"+action);
    }

    @Test
    public void testGetGroups() throws Exception{
        databaseController.clear();
        String groupName = new String("myGroup");
        ArrayList<String> group = new ArrayList<String>();
        group.add("x");
        group.add("y");
        group.add("z");
        groups.createGroup(groupName, group);
        try {Thread.sleep(200);}
        catch (Exception e) {}
        List<Map<String, Object>> groupJSON = mapper.convertValue(groupsRes.getGroups(), new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> members = mapper.convertValue(groupJSON.get(0).get("members"), new TypeReference<List<Map<String, Object>>>() {});
        try {Thread.sleep(200);}
        catch (Exception e) {}
        assertEquals(groupJSON.size(), 1);
        assertEquals(members.size(), 3);
    }

    @Test
    public void testCreateDeleteGroup() throws Exception{
        databaseController.clear();
        int nbrGroups = groups.getGroupsNames().size();
        String groupName = new String("myNewGroup");
        ArrayList<String> group = new ArrayList<String>();
        group.add("a");
        group.add("b");
        group.add("c");
        groupsRes.createGroup(groupName, group);
        try {Thread.sleep(200);}
        catch (Exception e) {}
        assertEquals(groups.getGroupsNames().size(), nbrGroups + 1);
        groupsRes.deleteGroup(groupName);
        try {Thread.sleep(200);}
        catch (Exception e) {}
        assertEquals(groups.getGroupsNames().size(), nbrGroups);
    }

    @Test
    public void testDeleteGroup() throws Exception{
        databaseController.clear();
        String groupName = new String("myNewGroup");
        ArrayList<String> group = new ArrayList<String>();
        group.add("a");
        group.add("b");
        group.add("c");
        groups.createGroup(groupName, group);
        try {Thread.sleep(200);}
        catch (Exception e) {}
        int nbrGroups = groups.getGroupsNames().size();
        groupsRes.deleteGroup(groupName);
        try {Thread.sleep(200);}
        catch (Exception e) {}
        assertEquals(groups.getGroupsNames().size(), nbrGroups-1);
    }
}
