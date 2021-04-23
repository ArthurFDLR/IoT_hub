package ece448.iot_sim.http_server;

import java.util.Map;

/**
 * Return a string upon a GET request.
 */
public interface RequestHandler {
    public String handleGet(String path, Map<String, String> params);
}
