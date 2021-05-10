package iot_sim;

import static org.junit.Assert.*;

import java.util.TreeMap;

import org.junit.Test;

public class PlugSimTests {

	@Test
	public void testInit() {
		PlugSim plug = new PlugSim("a");

		assertFalse(plug.isOn());
		assertTrue(plug.getPower() == 0);
	}

	@Test
	public void testSwitchOn() {
		PlugSim plug = new PlugSim("a");

		plug.switchOn();

		assertTrue(plug.isOn());
	}

	@Test
	public void testSwitchOff() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.switchOff();

		assertFalse(plug.isOn());
	}

	@Test
	public void testToggle() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.toggle();

		assertFalse(plug.isOn());
	}

	@Test
	public void testToggles() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.toggle();
		plug.toggle();

		assertTrue(plug.isOn());
	}

	@Test
	public void testName() {
		PlugSim plug = new PlugSim("a");
		assertTrue(plug.getName().equals("a"));
	}

	@Test
	public void testPowerOn() {
		PlugSim plug = new PlugSim("b.200");
		plug.switchOn();
		plug.measurePower();
		assertTrue(plug.getPower() == 200);
	}

	@Test
	public void testPowerSimulation() {
		PlugSim plug = new PlugSim("b");
		plug.switchOn();
		int i = 0;
		while (i<100){
			i++;
			plug.measurePower();
			assertTrue(plug.getPower() < 320); //300 + 40 - 20
			assertTrue(plug.getPower() > 0); //100 + 0 - 20
		}
	}

	@Test
	public void testPowerMeasureLimits() {
		PlugSim plug = new PlugSim("b");
		plug.switchOn();

		double initPower = Math.random() * 100;
		plug.updatePower(initPower);
		plug.measurePower();
		assertTrue(plug.getPower()>initPower);

		initPower = Math.random() * 20 + 300;
		plug.updatePower(initPower);
		plug.measurePower();
		assertTrue(plug.getPower()<initPower);
	}

	@Test
	public void testPowerOff() {
		PlugSim plug = new PlugSim("b.200");
		plug.switchOn();
		plug.measurePower();
		plug.switchOff();
		plug.measurePower();
		assertTrue(plug.getPower() == 0);
	}

	@Test
	public void testGlobalOff() {
		PlugSim plug = new PlugSim("cccccccc.1000");
		plug.switchOn();
		plug.measurePower();
		plug.toggle();
		plug.measurePower();
		assertTrue(plug.getName().equals("cccccccc.1000")
			&& !plug.isOn()
			&& (plug.getPower() == 0));
	}

	@Test
	public void testGlobalOn() {
		PlugSim plug = new PlugSim("cccccccc.1000");
		plug.switchOn();
		plug.measurePower();
		plug.switchOff();
		plug.toggle();
		plug.measurePower();
		assertTrue(plug.getName().equals("cccccccc.1000")
			&& plug.isOn()
			&& (plug.getPower() == 1000));
	}

	@Test
	public void testObserverInitOff() {
		String plugName = "a";
		PlugSim plug = new PlugSim(plugName);
		plug.switchOff();
		TreeMap<String, String> info = new TreeMap<String, String>();
		plug.addObserver((name, key, value) -> {
			assertEquals(plugName, name);
			info.put(key, value);
		});
		assertEquals("off", info.get("state"));
		assertEquals("0.000", info.get("power"));
	}

	@Test
	public void testObserverInitOn() {
		String plugName = "a";
		PlugSim plug = new PlugSim(plugName);
		plug.switchOn();
		TreeMap<String, String> info = new TreeMap<String, String>();
		plug.addObserver((name, key, value) -> {
			assertEquals(plugName, name);
			info.put(key, value);
		});
		assertEquals("on", info.get("state"));
		assertEquals(info.get("power"), String.format("%.3f", plug.getPower()));
	}

	@Test
	public void testObserverUpdate() {
		String plugName = "a.42";
		PlugSim plug = new PlugSim(plugName);
		TreeMap<String, String> info = new TreeMap<String, String>();
		plug.addObserver((name, key, value) -> {
			assertEquals(plugName, name);
			info.put(key, value);
		});
		plug.switchOn();
		assertEquals("on", info.get("state"));
		plug.measurePower();
		assertEquals("42.000", info.get("power"));
	}
}
