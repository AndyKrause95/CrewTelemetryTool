package crewtelemtetrytool.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MultiCastUDPClient {

	private DatagramSocket socket;
	private InetAddress group;
	private int expectedServerCount;
	private byte[] buf;

	public MultiCastUDPClient(int expectedServerCount) throws Exception {
		this.expectedServerCount = expectedServerCount;
		this.socket = new DatagramSocket();
		this.group = InetAddress.getByName("230.0.0.0");
	}
	
	 public String sendEcho(String msg) {
	        DatagramPacket packet = null;
	        try {
	            buf = msg.getBytes();
	            packet = new DatagramPacket(buf, buf.length, group, 4446);
	            socket.send(packet);
	            packet = new DatagramPacket(buf, buf.length);
	            socket.receive(packet);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        String received = new String(packet.getData(), 0, packet.getLength());
	        return received;
	    }

	public void close() {
		socket.close();
	}
}
