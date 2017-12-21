package actool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.swing.text.Document;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.w3c.dom.Attr;


class Handshake implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int identifier;
	int version;
	int operationId;

	public Handshake(int identifier, int version, int operationId) {
		this.identifier = identifier;
		this.version = version;
		this.operationId = operationId;
	}
}

public class UDPTest {
	public static DatagramSocket clientSocket;
	public static InetAddress IPAddress;
	public static final int AC_TELEMETRY_PORT = 9996;
	public static final String AC_TELEMETRY_IP = "localhost";

	public static void main(String[] args) throws ParserConfigurationException, TransformerException, NoSuchAlgorithmException {
		System.out.println("Start!");

		try {
			IPAddress = InetAddress.getByName(AC_TELEMETRY_IP);
			clientSocket = new DatagramSocket(9999);
			clientSocket.connect(IPAddress, AC_TELEMETRY_PORT);
			clientSocket.setSoTimeout(2000);

			int[] data = { 1, 1, 0 };

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(data);
			// byte[] sendData = outputStream.toByteArray();



			// ENDIAN STUFF
			ByteBuffer buf = ByteBuffer.allocate(12);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			buf.putInt(1);
			buf.putInt(1);
			buf.putInt(0);
			buf.rewind();

			//byte[] sendData = { (byte) 1, (byte) 1, (byte) 0 };

			// HANDSHAKE
			// DatagramPacket sendPacket = new DatagramPacket(sendData,
			// sendData.length);
			byte[] sendData = { 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
			//DatagramPacket sendPacket = new DatagramPacket(handshakeThing, handshakeThing.length);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
			clientSocket.send(sendPacket);

			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, 408);
			clientSocket.receive(receivePacket);

			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("HANDSHAKE: " + modifiedSentence);

			// -------------------------------------------------

			byte[] sub = { (byte) 1, (byte) 1, (byte) 1 };

			// ENDIAN STUFF
			ByteBuffer bufSub = ByteBuffer.allocate(12);
			bufSub.order(ByteOrder.LITTLE_ENDIAN);
			bufSub.putInt(1);
			bufSub.putInt(1);
			bufSub.putInt(1);
			bufSub.rewind();
			byte[] testArray = bufSub.array();
			// SUBSCRIBE
			byte[] sendDataSub = { 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0};
			sendPacket = new DatagramPacket(sendDataSub, sendDataSub.length);
			clientSocket.send(sendPacket);

			// receivePacket = new DatagramPacket(receiveData, 408);
			// clientSocket.receive(receivePacket);

			// modifiedSentence = new String(receivePacket.getData());
			System.out.println();
			// System.out.println("SUBSCRIBE: " + modifiedSentence);

			// ACTUAL DATA
			DatagramPacket lapData = new DatagramPacket(receiveData, 328);
			clientSocket.receive(lapData);

			String modifiedLapData = new String(lapData.getData());
			System.out.println();
			System.out.println("ACTUAL DATA: " + modifiedLapData);
			
			
			// ====== XML ======
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = docBuilder.newDocument();
			org.w3c.dom.Element rootElement = doc.createElement("telemetryRoot");
			doc.appendChild(rootElement);
			
			
			org.w3c.dom.Element carInfo = doc.createElement("CarInfo");
			rootElement.appendChild(carInfo);
			
			org.w3c.dom.Element carSpeed = doc.createElement("CarSpeed");
			carSpeed.appendChild(doc.createTextNode("200"));
			carInfo.appendChild(carSpeed);
			
			org.w3c.dom.Element trackInfo = doc.createElement("TrackInfo");
			rootElement.appendChild(trackInfo);
			
			org.w3c.dom.Element trackName = doc.createElement("TrackName");
			trackName.appendChild(doc.createTextNode("Test Name for a Track"));
			trackInfo.appendChild(trackName);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			String current = new java.io.File( "." ).getCanonicalPath();
			StreamResult result = new StreamResult(new File(current + "\\file.xml"));
			System.out.println("Current Path: " + current);
			
			
			StreamResult result2 = new StreamResult(System.out);
			
			transformer.transform(source, result);

			System.out.println("File saved!");
			
			
			System.out.println("Starting Websocket");
			ServerSocket socketTest = new ServerSocket(12345);
			System.out.println("Websocket Started");
			System.out.println("Waiting for Client");
			Socket client = socketTest.accept();
			System.out.println("Client connected");
			
			// ============== HANDSHAKE ===================
			
			String dataRequest = new Scanner(client.getInputStream(),"UTF-8").useDelimiter("\\r\\n\\r\\n").next();

			Matcher get = Pattern.compile("^GET").matcher(dataRequest);
			
			 Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(dataRequest);
			    match.find();
			    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
			            + "Connection: Upgrade\r\n"
			            + "Upgrade: websocket\r\n"
			            + "Sec-WebSocket-Accept: "
			            + DatatypeConverter
			            .printBase64Binary(
			                    MessageDigest
			                    .getInstance("SHA-1")
			                    .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
			                            .getBytes("UTF-8")))
			            + "\r\n\r\n")
			            .getBytes("UTF-8");

			    client.getOutputStream().write(response, 0, response.length);			
			
			// ============================================
			//dataRequest = new Scanner(client.getInputStream(),"UTF-8").useDelimiter("\\r\\n\\r\\n").next();
			System.out.println("Sending Data to Client");
			byte[] socketData = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
			//client.getOutputStream().write(socketData, 0, socketData.length);
			//client.getOutputStream().write("Test".getBytes("UTF-8"));
			
			

			System.out.println("Data sent");
			
			int i = 0;
			boolean loop = true;
			while(loop) {
				boolean flipVariable = true;
				if(flipVariable) {
					i++;
				} else {
					i--;
				}
				if(i >= 500) {
					flipVariable = false;
				} else if (i <= 0) {
					flipVariable = true;
				}
				String name = "TrackName : "+i;
				boolean isConnected = client.isConnected();
				if(isConnected) {
					System.out.println("Client is connected");
				} else {
					System.out.println("Client is not conncted");
				}
				
				//trackName.setTextContent(name);
				
				//result = new StreamResult(new File(current + "\\file.xml"));
				//result = new StreamResult(System.out);
				
				//transformer.transform(source, result);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
