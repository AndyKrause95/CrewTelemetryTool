package crewtelemetrytool.server.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import crewtelemetrytool.server.UDPMulticastServer;
import crewtelemtetrytool.client.MultiCastUDPClient;

public class ServerTest {
	MultiCastUDPClient client;
	
	@Before
    public void setup() throws Exception{
        new UDPMulticastServer().start();
        client = new MultiCastUDPClient(1);
    }
	
	@Test
	public void test() {
		String echo = client.sendEcho("hello server");
        assertEquals("hello server", echo);
        echo = client.sendEcho("server is working");
        assertFalse(echo.equals("hello server"));
	}
	
	@After
    public void tearDown() {
        client.sendEcho("end");
        client.close();
    }

}
