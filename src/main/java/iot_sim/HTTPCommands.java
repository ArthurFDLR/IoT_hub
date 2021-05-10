package iot_sim;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot_sim.http_server.RequestHandler;

public class HTTPCommands implements RequestHandler {

	// Use a map so we can search plugs by name.
	private final TreeMap<String, PlugSim> plugs = new TreeMap<>();

	public HTTPCommands(List<PlugSim> plugs) {
		for (PlugSim plug: plugs)
		{
			this.plugs.put(plug.getName(), plug);
		}
	}

	@Override
	public String handleGet(String path, Map<String, String> params) {
		// list all: /
		// do switch: /plugName?action=on|off|toggle
		// just report: /plugName

		logger.info("HTTPCmd {}: {}", path, params);

		if (path.equals("/"))
		{
			return listPlugs();
		}

		PlugSim plug = plugs.get(path.substring(1));
		if (plug == null)
			return null; // no such plug

		String action = params.get("action");
		if (action == null)
			return report(plug);

		switch (action) {
			case "toggle":
				plug.toggle();
				break;
			case "on":
				plug.switchOn();
				break;
			case "off":
				plug.switchOff();
				break;
			default:
				break;
		}

		String user_feedback = path.substring(1)
								+ " is "
								+ (plug.isOn()? "on": "off");

		return String.format("<html><body><p>%s</p>"
			+ "<p><a href='/%s'>Go back</a></p></body></html>",
			user_feedback,
			path.substring(1));
	}

	protected String listPlugs() {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><body>");
		for (String plugName: plugs.keySet())
		{
			sb.append(String.format("<p><a href='/%s'>%s</a></p>",
				plugName, plugName));
		}
		sb.append("</body></html>");

		return sb.toString();
	}

	protected String report(PlugSim plug) {
		String name = plug.getName();
		return String.format("<html><body>"
			+"<p>Plug %s is %s.</p>"
			+"<p>Power reading is %.3f.</p>"
			+"<p><a href='/%s?action=on'>Switch On</a></p>"
			+"<p><a href='/%s?action=off'>Switch Off</a></p>"
			+"<p><a href='/%s?action=toggle'>Toggle</a></p>"
			+"</body></html>",
			name,
			plug.isOn()? "on": "off",
			plug.getPower(), name, name, name);
	}

	private static final Logger logger = LoggerFactory.getLogger(HTTPCommands.class);
}
