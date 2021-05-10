package iot_sim;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

public class MQTTCommandsTests {

    private final ArrayList<PlugSim> plugs = new ArrayList<>();
    private final String topicPrefix = "unit/tests";

    public MQTTCommandsTests() {
        this.plugs.add(new PlugSim("Antenna"));
        this.plugs.add(new PlugSim("Bed.200"));
        this.plugs.add(new PlugSim("Computer"));
		}

    @Test
	public void testInit() {
        for (PlugSim plug : this.plugs)
        {
            assertFalse(plug.isOn());
            assertTrue(plug.getPower() == 0);
        }
    }

    @Test
	public void testTopic() {
        MQTTCommands command = new MQTTCommands(plugs, topicPrefix);
        assertEquals(topicPrefix+"/action/#", command.getTopic());
    }

    @Test
    public void testSimpleActions() {
        MQTTCommands command = new MQTTCommands(plugs, topicPrefix);
        plugs.get(0).switchOff();
        String name = plugs.get(0).getName();
        Boolean stateChecked = true;
        for (String action: Arrays.asList("on", "off", "toggle")){
            command.handleMessage(topicPrefix+"/action/"+name+"/"+action, new MqttMessage("".getBytes()));
            assertEquals(stateChecked, plugs.get(0).isOn());
            stateChecked = !stateChecked;
        }
    }

    @Test
    public void testNonExistantAction() {
        MQTTCommands command = new MQTTCommands(plugs, topicPrefix);
        plugs.get(0).switchOff();
        String name = plugs.get(0).getName();
        command.handleMessage(topicPrefix+"/action/"+name+"/non-existant", new MqttMessage("".getBytes()));
        assertFalse(plugs.get(0).isOn());
        command.handleMessage(topicPrefix+"/action/"+name+"/non/existant", new MqttMessage("".getBytes()));
        assertFalse(plugs.get(0).isOn());
    }

    @Test
    public void testNonExistantPlug() {
        MQTTCommands command = new MQTTCommands(plugs, topicPrefix);
        for (PlugSim plug: plugs){
            plug.switchOff();
        }
        command.handleMessage(topicPrefix+"/action/non-existant/on", new MqttMessage("".getBytes()));
        for (PlugSim plug: plugs){
            assertFalse(plug.isOn());
        }
    }

    @Test
    public void testNonAction() {
        MQTTCommands command = new MQTTCommands(plugs, topicPrefix);
        for (PlugSim plug: plugs){
            plug.switchOff();
        }
        command.handleMessage(topicPrefix+"/non-action/Antenna/on", new MqttMessage("".getBytes()));
        for (PlugSim plug: plugs){
            assertFalse(plug.isOn());
        }
    }
}