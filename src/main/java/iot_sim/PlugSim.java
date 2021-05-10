package iot_sim;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a smart plug with power monitoring.
 */
public class PlugSim {

	private final String name;
	private boolean on = false;
	private double power = 0; // in watts

	public PlugSim(String name) {
		logger.info("Create new plug: "+name);
		this.name = name;
	}

	/**
	 * No need to synchronize if read a final field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Switch the plug on.
	 */
	synchronized public void switchOn() {
		on = true;
		updateObservers("state","on");
	}

	/**
	 * Switch the plug off.
	 */
	synchronized public void switchOff() {
		on = false;
		updateObservers("state", "off");
	}

	/**
	 * Toggle the plug.
	 */
	synchronized public void toggle() {
		on = !on;
		updateObservers("state", on ? "on" : "off");
	}

	/**
	 * Measure power.
	 */
	synchronized public void measurePower() {
		if (!on) {
			updatePower(0);
			return;
		}

		// a trick to help testing
		if (name.indexOf(".") != -1)
		{
			updatePower(Integer.parseInt(name.split("\\.")[1]));
		}
		// do some random walk
		else if (power < 100)
		{
			updatePower(power + Math.random() * 100);
		}
		else if (power > 300)
		{
			updatePower(power - Math.random() * 100);
		}
		else
		{
			updatePower(power + Math.random() * 40 - 20);
		}
	}

	protected void updatePower(double p) {
		power = p;
		logger.debug("Plug {}: power {}", name, power);
		updateObservers("power", String.format("%.3f", power));
	}

	/**
	 * Getter: current state
	 */
	synchronized public boolean isOn() {
		return on;
	}

	/**
	 * Getter: last power reading
	 */
	synchronized public double getPower() {
		return power;
	}

	public void addObserver(Observer obs){
		observers.add(obs);
		obs.update(name, "state", isOn() ? "on" : "off");
		obs.update(name, "power", String.format("%.3f", power));
	}

	protected void updateObservers(String key, String value) {
		for (Observer obs: observers){
			obs.update(name, key, value);
		}
	}

	public static interface Observer {
		void update(String name, String key, String value);
	}

	private final ArrayList<Observer> observers = new ArrayList<>();

	private static final Logger logger = LoggerFactory.getLogger(PlugSim.class);
}
