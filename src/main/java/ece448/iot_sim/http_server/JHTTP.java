package ece448.iot_sim.http_server;

import java.net.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adopted from Java Network Programming 4th.
 * - Allow JHTTP to start in its own thread.
 * - Allow to stop JHTTP for grading.
 */
public class JHTTP {
	private static final Logger logger = LoggerFactory.getLogger(JHTTP.class);
	private final ServerSocket server;
	private final int port;
	private final RequestHandler handler;

	public JHTTP(int port, RequestHandler handler) throws Exception {
		this.server = new ServerSocket();
		this.port = port;
		this.handler = handler;
	}

	public void start() throws Exception {
		CountDownLatch c = new CountDownLatch(1);
		Thread t = new Thread(() -> loopForever(c));
		t.setDaemon(true);
		t.start();
		if (!c.await(60, TimeUnit.SECONDS))
			throw new Exception("JHTTP start timeout.");
	}

	public void close() throws Exception {
		server.close();
	}

	protected void loopForever(CountDownLatch c) {
		ExecutorService pool = Executors.newFixedThreadPool(50);
		try {
			server.setReuseAddress(true);
			server.bind(new InetSocketAddress(port));
			logger.info("JHTTP: accepting connections on port {}", server.getLocalPort());
			c.countDown();
			while (true) {
				Socket request = server.accept();
				Runnable r = new RequestProcessor(request, handler);
				pool.submit(r);
			}
		}
		catch (SocketException e) {
			logger.info("JHTTP: diconnnected {}", e.getMessage());
		}
		catch (Throwable th) {
			logger.error("JHTTP: exit", th);
			System.exit(-1);
		}
		finally {
			pool.shutdownNow();
		}
	}
}
