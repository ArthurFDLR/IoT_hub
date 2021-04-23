package ece448.iot_sim.http_server;

import java.io.*;
import java.net.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adopted from Java Network Programming 4th.
 * - Delegate to RequestHandler instead of returning files.
 */
public class RequestProcessor implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(RequestProcessor.class);
	private final Socket connection;
	private final RequestHandler handler;

	public RequestProcessor(Socket connection, RequestHandler handler) {
		this.connection = connection;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
			Writer out = new OutputStreamWriter(raw);
			Reader in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "US-ASCII");
			StringBuilder requestLine = new StringBuilder();
			while (true) {
				int c = in.read();
				if (c == -1)
					return;
				if (c == '\r' || c == '\n')
					break;
				requestLine.append((char) c);
			}

			String get = requestLine.toString();

			logger.info("JHTTP: {} {}", connection.getRemoteSocketAddress(), get);

			String[] tokens = get.split("\\s+");
			String method = tokens[0];
			String version = (tokens.length > 2) ? tokens[2] : "";
			if (method.equals("GET")) {
				String[] fields = tokens[1].split("\\?");
				String path = fields[0];
				HashMap<String, String> params = new HashMap<>();
				if (fields.length > 1) {
					for (String pair : fields[1].split("\\&")) {
						String[] kv = pair.split("=");
						params.put(kv[0], kv[1]);
					}
				}

				String rsp = handler.handleGet(path, params);
				if (rsp != null) {
					byte[] theData = rsp.getBytes("UTF-8");
					if (version.startsWith("HTTP/")) { // send a MIME header
						sendHeader(out, "HTTP/1.0 200 OK", "text/html", theData.length);
					}

					// send data; it may be an image or other binary data
					// so use the underlying output stream
					// instead of the writer
					raw.write(theData);
					raw.flush();
				} else { // can't find the file
					String body = new StringBuilder("<HTML>\r\n").append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
							.append("</HEAD>\r\n").append("<BODY>")
							.append("<H1>HTTP Error 404: File Not Found</H1>\r\n").append("</BODY></HTML>\r\n")
							.toString();
					if (version.startsWith("HTTP/")) { // send a MIME header
						sendHeader(out, "HTTP/1.0 404 File Not Found", "text/html; charset=utf-8", body.length());
					}
					out.write(body);
					out.flush();
				}
			} else { // method does not equal "GET"
				String body = new StringBuilder("<HTML>\r\n").append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
						.append("</HEAD>\r\n").append("<BODY>").append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
						.append("</BODY></HTML>\r\n").toString();
				if (version.startsWith("HTTP/")) { // send a MIME header
					sendHeader(out, "HTTP/1.0 501 Not Implemented", "text/html; charset=utf-8", body.length());
				}
				out.write(body);
				out.flush();
			}
		} catch (SocketException ex) {
			logger.warn("JHTTP: {} disconnected", connection.getRemoteSocketAddress());
		} catch (Throwable ex) {
			logger.warn("JHTTP: {} disconnected", connection.getRemoteSocketAddress(), ex);
		} finally {
			try {
				connection.close();
			} catch (IOException ex) {
			}
		}
	}

	private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
		out.write(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		out.write("Server: JHTTP2\r\n");
		out.write("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n\r\n");
		out.flush();
	}
}
