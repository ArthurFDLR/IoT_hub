package ece448.grading;

import ece448.iot_sim.PlugSim;

public class GradeP1 {
	public static void main(String[] args) {
		Grading.run(new GradeP1(), 10);
	}

	public boolean testCase00() {
		PlugSim plug = new PlugSim("a");
		return plug.getName().equals("a");
	}

	public boolean testCase01() {
		PlugSim plug = new PlugSim("a");
		return !plug.isOn();
	}

	public boolean testCase02() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		return plug.isOn();
	}

	public boolean testCase03() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.switchOff();
		return !plug.isOn();
	}
	
	public boolean testCase04() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.switchOff();
		plug.switchOn();
		return plug.isOn();
	}
	
	public boolean testCase05() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.switchOff();
		plug.switchOn();
		plug.toggle();
		return !plug.isOn();
	}

	public boolean testCase06() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		plug.switchOff();
		plug.switchOn();
		plug.toggle();
		plug.toggle();
		return plug.isOn();
	}

	public boolean testCase07() {
		PlugSim plug = new PlugSim("b.200");
		plug.switchOn();
		plug.measurePower();
		return plug.getPower() == 200;
	}

	public boolean testCase08() {
		PlugSim plug = new PlugSim("b.200");
		plug.switchOn();
		plug.measurePower();
		plug.switchOff();
		plug.measurePower();
		return plug.getPower() == 0;
	}

	public boolean testCase09() {
		PlugSim plug = new PlugSim("cccccccc.1000");
		plug.switchOn();
		plug.measurePower();
		plug.switchOff();
		plug.measurePower();
		return plug.getName().equals("cccccccc.1000")
			&& !plug.isOn();
	}
}
