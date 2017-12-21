package main;

import java.io.IOException;

public class CrewTelemetryTool {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Hello World!");
		//crewtelemetrytool.server.UDPMulticastServer serv = new crewtelemetrytool.server.UDPMulticastServer();
		//serv.run();
		crewtelemetrytool.server.WebSocketServer newServer = new crewtelemetrytool.server.WebSocketServer();
	}

}
