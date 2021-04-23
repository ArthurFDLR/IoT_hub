package ece448.iot_sim;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Take power measurements every 1 second.
 */
public class MeasurePower {

	private final List<PlugSim> plugs;

	public MeasurePower(List<PlugSim> plugs) {
		this.plugs = plugs;
	}

	public void start() {
		Thread t = new Thread(() -> {
			try
			{
				for (;;)
				{
					measureOnce();
				}
			}
			catch (Throwable th)
			{
				logger.error("Power: exit {}", th.getMessage(), th);
				System.exit(-1);
			}
		});

		// make sure this thread won't block JVM to exit
		t.setDaemon(true);

		// start measuring
		t.start();
	}

	/**
	 * Measure and wait 1s.
	 */
	protected void measureOnce() {
		try
		{
			for (PlugSim plug: plugs)
			{
				plug.measurePower();
			}

			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MeasurePower.class);
}
