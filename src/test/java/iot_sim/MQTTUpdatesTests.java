package iot_sim;

import static org.junit.Assert.*;

import org.junit.Test;

public class MQTTUpdatesTests {

    @Test
	public void testTopic() {
        MQTTUpdates update = new MQTTUpdates("prefix");
        assertEquals("prefix/update/name/key", update.getTopic("name", "key"));
    }

    @Test
	public void testMessage() {
        MQTTUpdates update = new MQTTUpdates("");
        String msg = "message";
        String msgBis = new String(update.getMessage(msg).getPayload());
        assertEquals(msg, msgBis);
    }
}
