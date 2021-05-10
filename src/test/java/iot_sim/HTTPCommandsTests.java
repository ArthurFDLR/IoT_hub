package iot_sim;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.TreeMap;

import static org.junit.Assert.*;

import org.junit.Test;

public class HTTPCommandsTests {

    private final ArrayList<PlugSim> plugs = new ArrayList<>();
    
    public HTTPCommandsTests() {
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
    public void testRootPath() {
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        command.handleGet("/", param);
        for (PlugSim plug : this.plugs)
        {
            assertFalse(plug.isOn());
            assertTrue(plug.getPower() == 0);
        }
    }

    @Test
    public void testNonExistantPlug() {
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        assertTrue(command.handleGet("/NonExistant", param) == null);
    }

    @Test
    public void testReport() {
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        for (PlugSim plug : this.plugs)
        {
            String name = plug.getName();
            assertTrue(command.handleGet("/"+name, param).contains(
                String.format("Power reading is %.3f.", plug.getPower())
                ));
            plug.switchOn();
            assertTrue(command.handleGet("/"+name, param).contains(name+" is on"));
            plug.switchOff();
            assertTrue(command.handleGet("/"+name, param).contains(name+" is off"));
        }
    }

    @Test
    public void testSwitchOn() {
        this.plugs.get(0).switchOff();
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        param.put("action", "on");
        String name = this.plugs.get(0).getName();
        assertTrue(command.handleGet("/"+name, param).contains(name + " is " + param.get("action")));
        assertTrue(this.plugs.get(0).isOn());
    }

    @Test
    public void testSwitchOff() {
        this.plugs.get(0).switchOn();
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        param.put("action", "off");
        String name = this.plugs.get(0).getName();
        assertTrue(command.handleGet("/"+name, param).contains(name + " is " + param.get("action")));
        assertFalse(this.plugs.get(0).isOn());
    }

    @Test
    public void testToggle() {
        this.plugs.get(0).switchOff();
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        param.put("action", "toggle");
        String name = this.plugs.get(0).getName();
        assertTrue(command.handleGet("/"+name, param).contains(name + " is on"));
        assertTrue(this.plugs.get(0).isOn());
        assertTrue(command.handleGet("/"+name, param).contains(name + " is off"));
        assertFalse(this.plugs.get(0).isOn());
    }

    @Test
    public void testNonExistantAction() {
        this.plugs.get(0).switchOff();
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        param.put("action", "non-existant");
        String name = this.plugs.get(0).getName();
        assertTrue(command.handleGet("/"+name, param).contains(name + " is off"));
        assertFalse(this.plugs.get(0).isOn());
    }

    @Test
    public void testMultipleCommands() {
        this.plugs.get(0).switchOff();
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        String name = this.plugs.get(0).getName();
        for (String order : Arrays.asList("on", "on", "off", "on", "off", "off"))
        {
            param.put("action", order);
            assertTrue(command.handleGet("/"+name, param).contains(name + " is " + order));
            if (order=="on")
                assertTrue(this.plugs.get(0).isOn());
            if (order=="off")
                assertFalse(this.plugs.get(0).isOn());
        }
        param.put("action", "toggle");
        assertTrue(command.handleGet("/"+name, param).contains(name + " is on"));
        assertTrue(this.plugs.get(0).isOn());
    }

    @Test
    public void testMultiplePlugs() {
        this.plugs.get(0).switchOff();
        HTTPCommands command = new HTTPCommands(plugs);
        TreeMap<String, String> param = new TreeMap<String, String>();
        String name = this.plugs.get(0).getName();
        param.put("action", "on");
        assertTrue(command.handleGet("/"+name, param).contains(name + " is on"));
        assertTrue(this.plugs.get(0).isOn() &
                   !this.plugs.get(1).isOn() &
                   !this.plugs.get(2).isOn());
        name = this.plugs.get(2).getName();
        param.put("action", "toggle");
        assertTrue(command.handleGet("/"+name, param).contains(name + " is on"));
        assertTrue(this.plugs.get(0).isOn() &
                   !this.plugs.get(1).isOn() &
                   this.plugs.get(2).isOn());
        name = this.plugs.get(0).getName();
        param.put("action", "off");
        assertTrue(command.handleGet("/"+name, param).contains(name + " is off"));
        assertTrue(!this.plugs.get(0).isOn() &
                   !this.plugs.get(1).isOn() &
                   this.plugs.get(2).isOn());
    }
}
