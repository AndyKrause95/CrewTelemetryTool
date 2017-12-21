package crewtelemetrytool.server;

import java.io.*;
import java.net.*;

public class UDPMulticastServer extends Thread {

	protected MulticastSocket socket = null;
	protected byte[] buf = new byte[256];
	protected InetAddress group = null;

	public UDPMulticastServer() throws IOException {
		socket = new MulticastSocket(4446);
		socket.setReuseAddress(true);
		group = InetAddress.getByName("230.0.0.0");
		socket.joinGroup(group);
	}

	public void run() {
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				packet = new DatagramPacket(buf, buf.length, address, port);
				String received = new String(packet.getData(), 0, packet.getLength());
				if (received.equals("end")) {
					break;
				}
				socket.send(packet);
			}
			socket.leaveGroup(group);
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
