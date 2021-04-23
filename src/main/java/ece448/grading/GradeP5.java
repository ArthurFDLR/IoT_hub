package ece448.grading;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.SimConfig;
import ece448.grading.GradeP3.MqttController;
import ece448.iot_hub.HubConfig;

public class GradeP5 implements AutoCloseable {

	private static final String broker = "tcp://127.0.0.1";
	private static final String topicPrefix = System.currentTimeMillis()+"/grade_p5/iot_ece448";
	private static final List<String> plugNames = Arrays.asList("a", "b", "c");
	private static final List<String> plugNamesEx = Arrays.asList("d", "e", "f", "g");
	private static final List<String> groupNames = Arrays.asList("x", "y", "z");

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(GradeP5.class);

	private final MqttController mqtt;

	private GradeP5() throws Exception {
		this.mqtt = new MqttController(broker, "grader/iot_hub", topicPrefix);
		this.mqtt.start();
	}

	@Override
	public void close() throws Exception {
		mqtt.close();
	}

	public static void main(String[] args) throws Exception {
		SimConfig config = new SimConfig(8080, plugNames, broker, "testee/iot_sim", topicPrefix);
		SimConfig configEx = new SimConfig(8081, plugNamesEx, broker, "ex_testee/iot_sim", topicPrefix);
		HubConfig hubConfig = new HubConfig(8088, broker, "testee/iot_hub", topicPrefix);

		try (
			GradeP5 p5 = new GradeP5();
			ece448.iot_sim.Main m = new ece448.iot_sim.Main(config);
			ece448.iot_sim.Main mex = new ece448.iot_sim.Main(configEx);
			ece448.iot_hub.Main hub = new ece448.iot_hub.Main(hubConfig, new String[0]))
		{
			Grading.run(p5, 10);
		}
	}

	static void postGroup(String group, List<String> members) throws Exception {
		Request.Post("http://127.0.0.1:8088/api/groups/" + group)
			.bodyByteArray(mapper.writeValueAsBytes(members), ContentType.APPLICATION_JSON)
			.userAgent("Mozilla/5.0").connectTimeout(1000)
			.socketTimeout(1000).execute();
	}

	static void delGroup(String group) throws Exception {
		Request.Delete("http://127.0.0.1:8088/api/groups/" + group)
			.userAgent("Mozilla/5.0").connectTimeout(1000)
			.socketTimeout(1000).execute();
	}

	static String getGroups1() throws Exception {
		TreeMap<String, String> fields = new TreeMap<>();

		for (String name: groupNames)
		{
			Map<String, Object> group = mapper.readValue(GradeP4.getHub("/api/groups/"+name),
				new TypeReference<Map<String, Object>>() {});
			if (!name.equals((String)group.get("name")))
				throw new Exception("invalid name " + name);

			StringBuilder field = new StringBuilder(name+".");
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> members = (List<Map<String, Object>>)group.get("members");
			for (Map<String, Object> member: members)
			{
				field.append(member.get("name"));
				field.append("off".equals(member.get("state"))? "0": "1");
			}
			if (!members.isEmpty())
				fields.put(name, field.toString());
		}
		String ret = String.join("|", fields.values());
		logger.debug("GradeP5: getGroups1 {}", ret);
		return ret;
	}

	static String getGroups2() throws Exception {
		TreeMap<String, String> fields = new TreeMap<>();

		List<Map<String, Object>> groups = mapper.readValue(GradeP4.getHub("/api/groups"),
			new TypeReference<List<Map<String, Object>>>() {});
		for (Map<String, Object> group: groups)
		{
			String name = (String)group.get("name");
			StringBuilder field = new StringBuilder(name+".");
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> members = (List<Map<String, Object>>)group.get("members");
			for (Map<String, Object> member: members)
			{
				field.append(member.get("name"));
				field.append("off".equals(member.get("state"))? "0": "1");
			}
			fields.put(name, field.toString());
		}
		String ret = String.join("|", fields.values());
		logger.debug("GradeP5: getGroups2 {}", ret);
		return ret;
	}

	static boolean verifyGroups(String groups) throws Exception {
		return groups.equals(getGroups1())
			&& groups.equals(getGroups2());
	}

	public boolean testCase00() throws Exception {
		return verifyGroups("");
	}

	public boolean testCase01() throws Exception {
		GradeP4.getHub("/api/plugs/a?action=off");
		GradeP4.getHub("/api/plugs/b?action=on");
		GradeP4.getHub("/api/plugs/c?action=off");
		GradeP4.getHub("/api/plugs/d?action=toggle");
		GradeP4.getHub("/api/plugs/e?action=on");
		GradeP4.getHub("/api/plugs/f?action=off");
		GradeP4.getHub("/api/plugs/g?action=off");

		Thread.sleep(1000);
		return GradeP4.verifyStates("0101100", mqtt) && verifyGroups("");
	}

	public boolean testCase02() throws Exception {
		postGroup("z", Arrays.asList("a", "d"));

		Thread.sleep(1000);
		return GradeP4.verifyStates("0101100", mqtt)
			&& verifyGroups("z.a0d1");
	}

	public boolean testCase03() throws Exception {
		postGroup("y", Arrays.asList("b", "d", "f"));

		Thread.sleep(1000);
		return GradeP4.verifyStates("0101100", mqtt)
			&& verifyGroups("y.b1d1f0|z.a0d1");
	}

	public boolean testCase04() throws Exception {
		postGroup("x", Arrays.asList("a", "c", "e", "g"));

		Thread.sleep(1000);
		return GradeP4.verifyStates("0101100", mqtt)
			&& verifyGroups("x.a0c0e1g0|y.b1d1f0|z.a0d1");
	}

	public boolean testCase05() throws Exception {
		GradeP4.getHub("/api/groups/x?action=on");
		GradeP4.getHub("/api/groups/y?action=off");

		Thread.sleep(1000);
		return GradeP4.verifyStates("1010101", mqtt)
			&& verifyGroups("x.a1c1e1g1|y.b0d0f0|z.a1d0");
	}

	public boolean testCase06() throws Exception {
		GradeP4.getHub("/api/groups/z?action=toggle");

		Thread.sleep(1000);
		return GradeP4.verifyStates("0011101", mqtt)
			&& verifyGroups("x.a0c1e1g1|y.b0d1f0|z.a0d1");
	}

	public boolean testCase07() throws Exception {
		GradeP4.getSim("/c?action=off");
		GradeP4.getSimEx("/d?action=off");
		mqtt.publishAction("e", "off");
		mqtt.publishAction("g", "toggle");

		Thread.sleep(1000);
		return GradeP4.verifyStates("0000000", mqtt)
			&& verifyGroups("x.a0c0e0g0|y.b0d0f0|z.a0d0");
	}

	public boolean testCase08() throws Exception {
		delGroup("z");

		Thread.sleep(1000);
		return GradeP4.verifyStates("0000000", mqtt)
			&& verifyGroups("x.a0c0e0g0|y.b0d0f0");
	}

	public boolean testCase09() throws Exception {
		postGroup("x", Arrays.asList("a", "b", "c"));
		postGroup("y", Arrays.asList("e", "f", "g"));

		Thread.sleep(500);
		GradeP4.getHub("/api/groups/x?action=toggle");
		GradeP4.getHub("/api/groups/y?action=toggle");
		GradeP4.getHub("/api/groups/x?action=toggle");

		Thread.sleep(1000);
		return GradeP4.verifyStates("0000111", mqtt)
			&& verifyGroups("x.a0b0c0|y.e1f1g1");
	}
}
