package crewtelemetrytool.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

public class WebSocketServer {

	public WebSocketServer() throws IOException, NoSuchAlgorithmException {
		System.out.println("Starting Websocket");
		ServerSocket socketTest = new ServerSocket(12345);
		System.out.println("Websocket Started");
		System.out.println("Waiting for Client");
		Socket client = socketTest.accept();
		System.out.println("Client connected");

		String dataRequest = new Scanner(client.getInputStream(), "UTF-8")
				.useDelimiter("\\r\\n\\r\\n").next();

		Matcher get = Pattern.compile("^GET").matcher(dataRequest);

		Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(
				dataRequest);
		match.find();
		byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
				+ "Connection: Upgrade\r\n"
				+ "Upgrade: websocket\r\n"
				+ "Sec-WebSocket-Accept: "
				+ DatatypeConverter
						.printBase64Binary(MessageDigest
								.getInstance("SHA-1")
								.digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
										.getBytes("UTF-8"))) + "\r\n\r\n")
				.getBytes("UTF-8");

		client.getOutputStream().write(response, 0, response.length);

		System.out.println("Sending Data to Client");
		byte[] socketData = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
		client.getOutputStream().write(socketData, 0, socketData.length);

	}

}
